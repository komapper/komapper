package org.komapper.processor.command

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import org.komapper.annotation.KomapperPartial
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
import org.komapper.processor.getClassDeclaration
import org.komapper.processor.hasAnnotation
import org.komapper.processor.report
import org.komapper.processor.resolveTypeArgumentsOfAncestor
import kotlin.reflect.KClass

internal class CommandFactory(
    private val context: Context,
    private val annotationClass: KClass<*>,
    private val symbol: KSAnnotated,
) {
    private val longTypeArg by lazy {
        val longType = context.resolver.builtIns.longType
        val typeRef = context.resolver.createKSTypeReferenceFromKSType(longType)
        context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
    }

    fun create(): Command {
        val classDeclaration = symbol as? KSClassDeclaration
            ?: report("The annotated element is not a class.", symbol)
        when (classDeclaration.classKind) {
            ClassKind.CLASS -> Unit
            else -> report("The annotated element must be a class.", symbol)
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
        val (kind, returnType) = findKindAndReturnType(classDeclaration) ?: report(
            "${classDeclaration.qualifiedName?.asString()} must extend one of the following classes: One, Many, Exec, ExecReturnOne, or ExecReturnMany.",
            symbol,
        )
        val sqlPartialMap = buildSqlPartialMap(classDeclaration)
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
            sqlPartialMap = sqlPartialMap,
            packageName = packageName,
            fileName = fileName,
        )
    }

    private fun buildSqlPartialMap(classDeclaration: KSClassDeclaration): Map<String, String> {
        val file = classDeclaration.containingFile ?: return emptyMap()
        return file.declarations
            .mapNotNull { it as? KSPropertyDeclaration }
            .filter { Modifier.CONST in it.modifiers }
            .map {
                val value = it.findAnnotation(KomapperPartial::class)
                    ?.findValue("sql")
                    ?.toString()
                    ?.trimIndent() ?: ""
                it.simpleName.asString() to value
            }
            .toMap()
    }

    private fun findKindAndReturnType(classDeclaration: KSClassDeclaration): Pair<CommandKind, KSType>? {
        val descendantType = classDeclaration.asType(emptyList())
        for (kind in CommandKind.values()) {
            val ancestorType = context.getClassDeclaration(kind.className, symbol).asStarProjectedType()
            if (kind == EXEC && ancestorType.isAssignableFrom(descendantType)) {
                return kind to createType(Query::class, listOf(longTypeArg))
            }
            val typeArgs = resolveTypeArgumentsOfAncestor(descendantType, ancestorType)
            if (typeArgs.isEmpty()) continue
            if (typeArgs.any { it == null }) report("Cannot get type argument of ${kind.className}.", symbol)
            val nonNullTypeArgs = typeArgs.filterNotNull()
            if (nonNullTypeArgs.any { it.variance == Variance.STAR }) {
                report("Specifying a star projection for ${kind.className} is not supported.", symbol)
            }
            return kind to when (kind) {
                ONE -> createType(Query::class, nonNullTypeArgs)
                MANY -> createType(ListQuery::class, nonNullTypeArgs)
                EXEC_RETURN_ONE -> createType(Query::class, nonNullTypeArgs)
                EXEC_RETURN_MANY -> createType(ListQuery::class, nonNullTypeArgs)
                EXEC -> error("unreachable. descendant=$descendantType, ancestor=$ancestorType")
            }
        }
        return null
    }

    private fun createType(klass: KClass<*>, typeArgs: List<KSTypeArgument>): KSType {
        return context.getClassDeclaration(klass.qualifiedName!!, symbol).asType(typeArgs)
    }

    private fun createFileName(context: Context, classDeclaration: KSClassDeclaration): Pair<String, String> {
        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
        return packageName to simpleName
    }
}
