package org.komapper.processor.command

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import org.komapper.annotation.KomapperUnused
import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query
import org.komapper.processor.Context
import org.komapper.processor.command.CommandKind.EXEC
import org.komapper.processor.command.CommandKind.MANY
import org.komapper.processor.command.CommandKind.ONE
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
        if (classDeclaration.typeParameters.isNotEmpty()) {
            report("The class with type parameters is not supported.", symbol)
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
        val name = annotation.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrEmpty()) "execute" else it
        }
        val disableValidation = annotation.findValue("disableValidation")?.toString()?.toBooleanStrict()
            ?: report("The annotation value \"disableValidation\" is not found.", annotation)
        val result = createCommandResult(classDeclaration)
        val (packageName, fileName) = createFileName(context, classDeclaration)
        return Command(sql, disableValidation, annotation, classDeclaration, name, paramMap, unusedParams, result, packageName, fileName)
    }

    // TODO refactor
    private fun createCommandResult(classDeclaration: KSClassDeclaration): CommandResult {
        val (kind, type, typeArgs) = findCommandKSType(classDeclaration, emptyMap()) ?: report(
            "${classDeclaration.qualifiedName?.asString()} must extend or implement one of the following classes or interfaces: " +
                "One, Many, Exec, FetchOne, FetchMany, or ExecChange.",
            classDeclaration,
        )
        return when (kind) {
            ONE -> {
                val returnType = createType(Query::class, typeArgs)
                CommandResult(ONE, returnType, "from")
            }

            MANY -> {
                val returnType = createType(ListQuery::class, typeArgs)
                CommandResult(MANY, returnType, "from")
            }

            EXEC -> {
                val longType = context.resolver.builtIns.longType
                val typeRef = context.resolver.createKSTypeReferenceFromKSType(longType)
                val typeArg = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
                val returnType = createType(Query::class, listOf(typeArg))
                CommandResult(EXEC, returnType, "execute")
            }
        }
    }

    private fun createType(klass: KClass<*>, typeArgs: List<KSTypeArgument>): KSType {
        return context.resolver.getKSNameFromString(klass.qualifiedName!!).let {
            context.resolver.getClassDeclarationByName(it)?.asType(typeArgs)
                ?: report("Class not found: ${it.asString()}", symbol)
        }
    }

    private fun findCommandKSType(classDeclaration: KSClassDeclaration, map: Map<KSTypeParameter, KSTypeArgument>): Triple<CommandKind, KSType, List<KSTypeArgument>>? {
        for (superType in classDeclaration.superTypes) {
            val type = superType.resolve()
            val declaration = type.declaration as? KSClassDeclaration ?: continue
            val name = declaration.qualifiedName?.asString()
            val kind = when (name) {
                ONE.interfaceName, ONE.abstractClasName -> ONE
                MANY.interfaceName, MANY.abstractClasName -> MANY
                EXEC.interfaceName, EXEC.abstractClasName -> EXEC
                else -> null
            }
            return if (kind != null) {
                // resolve type arguments
                val args = type.arguments.asSequence()
                    .map { arg -> arg to arg.type?.resolve()?.declaration }
                    .map { (type, decl) -> if (decl == null) type to null else type to map[decl] }
                    .map { (original, resolved) -> resolved ?: original }
                    .toList()
                Triple(kind, type, args)
            } else {
                val newMap = map + declaration.typeParameters.zip(type.arguments).toMap()
                findCommandKSType(declaration, newMap)
            }
        }
        return null
    }

    private fun createFileName(context: Context, classDeclaration: KSClassDeclaration): Pair<String, String> {
        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
        return packageName to simpleName
    }
}
