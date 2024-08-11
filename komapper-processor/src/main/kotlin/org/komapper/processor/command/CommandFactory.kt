package org.komapper.processor.command

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import org.komapper.annotation.KomapperUnused
import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.processor.Context
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue
import org.komapper.processor.hasAnnotation
import org.komapper.processor.report
import kotlin.reflect.KClass

internal class CommandFactory(
    private val context: Context,
    private val annotationClass: KClass<*>,
    private val symbol: KSAnnotated,
) {
    fun create(): Command {
        val classDeclaration = symbol as? KSClassDeclaration
            ?: report("The annotated element is not a class.", symbol)
        when (classDeclaration.classKind) {
            ClassKind.CLASS, ClassKind.OBJECT, ClassKind.INTERFACE -> Unit
            else -> report("The annotated element must be either a class, an object, or an interface.", symbol)
        }
        val properties = classDeclaration.getAllProperties()
            .filterNot { Modifier.PRIVATE in it.modifiers }
            .toList()
        val paramMap = properties.associate { it.simpleName.asString() to it.type.resolve() }
        val unusedParams = properties.filter { it.hasAnnotation(KomapperUnused::class) }.map { it.simpleName.asString() }.toSet()
        val annotation = classDeclaration.findAnnotation(annotationClass)
            ?: report("The annotation \"${annotationClass.simpleName}\" is not found.", symbol)
        val sql = annotation.findValue("sql")?.toString()?.trimIndent()
            ?: report("The annotation value \"sql\" is not found.", annotation)
        val disableValidation = annotation.findValue("disableValidation")?.toString()?.toBooleanStrict()
            ?: report("The annotation value \"disableValidation\" is not found.", annotation)
        val result = createCommandResult(classDeclaration)
        val (packageName, fileName) = createFileName(context, classDeclaration)
        return Command(sql, disableValidation, annotation, classDeclaration, paramMap, unusedParams, result, packageName, fileName)
    }

    private fun createCommandResult(classDeclaration: KSClassDeclaration): CommandResult {
        for (type in classDeclaration.getAllSuperTypes()) {
            val markerInterfaceName = type.declaration.qualifiedName?.asString()
            when (markerInterfaceName) {
                CommandKind.ONE.markerInterfaceName -> {
                    val returnType = context.resolver.getKSNameFromString(Query::class.qualifiedName!!).let {
                        context.resolver.getClassDeclarationByName(it)?.asType(type.arguments)
                            ?: report("Class not found: ${it.asString()}", classDeclaration)
                    }
                    return CommandResult(CommandKind.ONE, returnType, "from")
                }

                CommandKind.MANY.markerInterfaceName -> {
                    val returnType = context.resolver.getKSNameFromString(ListQuery::class.qualifiedName!!).let {
                        context.resolver.getClassDeclarationByName(it)?.asType(type.arguments)
                            ?: report("Class not found: ${it.asString()}", classDeclaration)
                    }
                    return CommandResult(CommandKind.MANY, returnType, "from")
                }

                CommandKind.EXEC.markerInterfaceName -> {
                    val returnType = context.resolver.getKSNameFromString(Query::class.qualifiedName!!).let {
                        val longType = context.resolver.builtIns.longType
                        val typeRef = context.resolver.createKSTypeReferenceFromKSType(longType)
                        val typeArg = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
                        context.resolver.getClassDeclarationByName(it)?.asType(listOf(typeArg))
                            ?: report("Class not found: ${it.asString()}", classDeclaration)
                    }
                    return CommandResult(CommandKind.EXEC, returnType, "execute")
                }

                else -> Unit
            }
        }
        val name = classDeclaration.qualifiedName?.asString()
        report("$name must implement one of the interfaces: One, Many, or Exec.", classDeclaration)
    }

    private fun createFileName(context: Context, classDeclaration: KSClassDeclaration): Pair<String, String> {
        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
        return packageName to simpleName
    }
}
