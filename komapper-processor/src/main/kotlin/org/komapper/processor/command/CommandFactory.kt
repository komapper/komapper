package org.komapper.processor.command

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Variance
import org.komapper.annotation.KomapperCommand
import org.komapper.core.Exec
import org.komapper.core.Many
import org.komapper.core.One
import org.komapper.processor.Context
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue
import org.komapper.processor.report

internal class CommandFactory(
    private val context: Context,
    private val symbol: KSAnnotated,
) {
    fun create(): Command {
        val classDeclaration = symbol as? KSClassDeclaration
            ?: report("The annotated element is not a class.", symbol)
        when (classDeclaration.classKind) {
            ClassKind.CLASS, ClassKind.OBJECT -> Unit
            else -> report("The annotated element must be a class or an object.", symbol)
        }
        val parameters = classDeclaration.primaryConstructor?.parameters ?: emptyList()
        val annotation = classDeclaration.findAnnotation(KomapperCommand::class)
            ?: report("The annotation @KomapperCommand is not found.", symbol)
        val sql = annotation.findValue("sql")?.toString()
            ?: report("The value of 'sql' is not found.", annotation)
        val disableValidation = annotation.findValue("disableValidation")?.toString()?.toBooleanStrict()
            ?: report("The value of 'disableValidation' is not found.", annotation)
        val result = createCommandResult(classDeclaration)
        val (packageName, fileName) = createFileName(context, classDeclaration)
        return Command(sql, disableValidation, annotation, classDeclaration, parameters, result, packageName, fileName)
    }

    private fun createCommandResult(classDeclaration: KSClassDeclaration): CommandResult {
        for (superType in classDeclaration.superTypes) {
            val type = superType.resolve()
            val name = type.declaration.qualifiedName?.asString()
            when (name) {
                One::class.qualifiedName!! -> {
                    val returnType = context.resolver.getKSNameFromString("org.komapper.core.dsl.query.Query").let {
                        context.resolver.getClassDeclarationByName(it)?.asType(type.arguments) ?: error(it.asString())
                    }
                    return CommandResult(CommandKind.One, returnType, "from")
                }

                Many::class.qualifiedName!! -> {
                    val returnType = context.resolver.getKSNameFromString("org.komapper.core.dsl.query.ListQuery").let {
                        context.resolver.getClassDeclarationByName(it)?.asType(type.arguments) ?: error(it.asString())
                    }
                    return CommandResult(CommandKind.Many, returnType, "from")
                }

                Exec::class.qualifiedName!! -> {
                    val returnType = context.resolver.getKSNameFromString("org.komapper.core.dsl.query.Query").let {
                        val longType = context.resolver.builtIns.longType
                        val typeRef = context.resolver.createKSTypeReferenceFromKSType(longType)
                        val argument = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
                        context.resolver.getClassDeclarationByName(it)?.asType(listOf(argument)) ?: error(it.asString())
                    }
                    return CommandResult(CommandKind.Exec, returnType, "execute")
                }

                else -> Unit
            }
        }
        val name = classDeclaration.qualifiedName?.asString()
        report("$name must directly implement one of the interfaces: One, Many, or Exec.", classDeclaration)
    }

    private fun createFileName(context: Context, classDeclaration: KSClassDeclaration): Pair<String, String> {
        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
        return packageName to simpleName
    }
}
