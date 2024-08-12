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
import org.komapper.processor.command.CommandKind.EXEC_RETURN_MANY
import org.komapper.processor.command.CommandKind.EXEC_RETURN_ONE
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
    private val commandKindMap = CommandKind.values().associateBy { it.className }
    private val longTypeArg by lazy {
        val longType = context.resolver.builtIns.longType
        val typeRef = context.resolver.createKSTypeReferenceFromKSType(longType)
        context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
    }

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
        val functionName = annotation.findValue("functionName")?.toString()?.trim().let {
            if (it.isNullOrEmpty()) "execute" else it
        }
        val disableValidation = annotation.findValue("disableValidation")?.toString()?.toBooleanStrict()
            ?: report("The annotation value \"disableValidation\" is not found.", annotation)
        val (kind, returnType) = findKindAndReturnType(classDeclaration, emptyMap())
        val (packageName, fileName) = createFileName(context, classDeclaration)
        return Command(
            kind = kind,
            sql = sql,
            disableValidation = disableValidation,
            annotation = annotation,
            classDeclaration = classDeclaration,
            functionName = functionName,
            paramMap = paramMap,
            unusedParams = unusedParams,
            returnType = returnType,
            packageName = packageName,
            fileName = fileName,
        )
    }

    private fun findKindAndReturnType(classDeclaration: KSClassDeclaration, map: Map<KSTypeParameter, KSTypeArgument>): Pair<CommandKind, KSType> {
        for (superType in classDeclaration.superTypes) {
            val type = superType.resolve()
            val declaration = type.declaration as? KSClassDeclaration ?: continue
            val name = declaration.qualifiedName?.asString()
            val kind = commandKindMap[name]
            return if (kind != null) {
                // resolve type arguments
                val typeArgs = type.arguments.asSequence()
                    .map { arg -> arg to arg.type?.resolve()?.declaration }
                    .map { (type, decl) -> if (decl == null) type to null else type to map[decl] }
                    .map { (original, resolved) -> resolved ?: original }
                    .toList()
                when (kind) {
                    ONE -> ONE to createType(Query::class, typeArgs)
                    MANY -> MANY to createType(ListQuery::class, typeArgs)
                    EXEC -> EXEC to createType(Query::class, listOf(longTypeArg))
                    EXEC_RETURN_ONE -> EXEC_RETURN_ONE to createType(Query::class, typeArgs)
                    EXEC_RETURN_MANY -> EXEC_RETURN_MANY to createType(ListQuery::class, typeArgs)
                }
            } else {
                val newMap = map + declaration.typeParameters.zip(type.arguments).toMap()
                findKindAndReturnType(declaration, newMap)
            }
        }
        val name = classDeclaration.qualifiedName?.asString()
        report(
            "$name must extend one of the following classes: One, Many, Exec, ExecReturnOne, or ExecReturnMany.",
            symbol,
        )
    }

    private fun createType(klass: KClass<*>, typeArgs: List<KSTypeArgument>): KSType {
        return context.resolver.getKSNameFromString(klass.qualifiedName!!).let {
            context.resolver.getClassDeclarationByName(it)?.asType(typeArgs)
                ?: report("Class not found: ${it.asString()}", symbol)
        }
    }

    private fun createFileName(context: Context, classDeclaration: KSClassDeclaration): Pair<String, String> {
        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
        return packageName to simpleName
    }
}
