package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import java.nio.CharBuffer
import kotlin.reflect.KClass

internal fun <T> Iterable<T>.hasDuplicates(predicate: (T) -> Boolean): Boolean {
    return this.filter(predicate).take(2).count() == 2
}

internal fun KSClassDeclaration.isValueClass(): Boolean {
    return if (this.modifiers.contains(Modifier.VALUE)) {
        true
    } else {
        // TODO: Remove this condition.
        // The VALUE modifier is not found in KSP 2.
        // Check for the presence of the INLINE modifier until this issue is fixed.
        this.modifiers.contains(Modifier.INLINE)
    }
}

internal fun KSAnnotation.findValue(name: String): Any? {
    return this.arguments
        .filter { it.name?.asString() == name }
        .map { it.value }
        .firstOrNull()
}

internal fun KSAnnotated.findAnnotations(klass: KClass<*>): List<KSAnnotation> {
    return this.annotations.filter { it.shortName.asString() == klass.simpleName }.toList()
}

internal fun KSAnnotated.findAnnotation(klass: KClass<*>): KSAnnotation? {
    return this.annotations.firstOrNull { it.shortName.asString() == klass.simpleName }
}

internal fun KSAnnotated.findAnnotation(simpleName: String): KSAnnotation? {
    return this.annotations.firstOrNull { it.shortName.asString() == simpleName }
}

internal fun KSAnnotated.hasAnnotation(klass: KClass<*>): Boolean {
    return findAnnotation(klass) != null
}

internal fun KSType.normalize(parent: TypeArgumentResolver = TypeArgumentResolver()): Pair<KSType, TypeArgumentResolver> {
    return this.declaration.accept(
        object : KSEmptyVisitor<Unit, Pair<KSType, TypeArgumentResolver>>() {
            override fun defaultHandler(node: KSNode, data: Unit): Pair<KSType, TypeArgumentResolver> {
                val resolver =
                    TypeArgumentResolver(parent, this@normalize.declaration.typeParameters, this@normalize.arguments)
                return this@normalize to resolver
            }

            override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): Pair<KSType, TypeArgumentResolver> {
                val resolver = TypeArgumentResolver(parent, typeAlias.typeParameters, this@normalize.arguments)
                val type = typeAlias.type.resolve().let {
                    if (this@normalize.isMarkedNullable) {
                        it.makeNullable()
                    } else {
                        it
                    }
                }
                return type.normalize(resolver)
            }
        },
        Unit,
    )
}

internal class TypeArgumentResolver(
    private val parent: TypeArgumentResolver? = null,
    typeParameters: List<KSTypeParameter> = emptyList(),
    typeArguments: List<KSTypeArgument> = emptyList(),
) {
    private val context = typeParameters.map { it.name.asString() }.zip(typeArguments).toMap()

    fun resolve(declaration: KSDeclaration): KSTypeArgument? {
        val typeArgument = context[declaration.simpleName.asString()]
        return if (typeArgument != null) {
            if (parent != null) {
                val typeParameter = typeArgument.type?.resolve()?.declaration as? KSTypeParameter
                if (typeParameter != null) {
                    parent.resolve(typeParameter) ?: typeArgument
                } else {
                    typeArgument
                }
            } else {
                typeArgument
            }
        } else {
            null
        }
    }
}

internal val KSType.name: String
    get() {
        fun asString(): String {
            return (declaration.qualifiedName ?: declaration.simpleName).asString()
        }

        return createTypeName(this, asString())
    }

internal val KSType.backquotedName: String
    get() {
        fun asString(): String {
            return createBackquotedName(declaration)
        }

        return createTypeName(this, asString())
    }

internal fun createBackquotedName(declaration: KSDeclaration): String {
    val qualifiedName = declaration.qualifiedName?.asString()
    return if (qualifiedName == null) {
        declaration.simpleName.asString()
    } else {
        val packageName = declaration.packageName.asString()
        if (packageName.isEmpty()) {
            qualifiedName
        } else {
            val remains = qualifiedName.substring(packageName.length)
            "`$packageName`$remains"
        }
    }
}

private fun createTypeName(type: KSType, baseName: String): String {
    val buf = StringBuilder()
    buf.append(baseName)
    if (type.arguments.isNotEmpty()) {
        buf.append("<")
        type.arguments.joinTo(buf) {
            val t = it.type?.resolve()
            if (t == null) {
                it.variance.label
            } else {
                val mark = if (t.nullability == Nullability.NULLABLE) "?" else ""
                t.name + mark
            }
        }
        buf.append(">")
    }
    return buf.toString()
}

internal fun toCamelCase(text: String): String {
    val builder = StringBuilder()
    val buf = CharBuffer.wrap(text)
    if (buf.hasRemaining()) {
        builder.append(buf.get().lowercaseChar())
    }
    while (buf.hasRemaining()) {
        val c1 = buf.get()
        buf.mark()
        if (buf.hasRemaining()) {
            val c2 = buf.get()
            if (c1.isUpperCase() && c2.isLowerCase()) {
                builder.append(c1).append(c2).append(buf)
                break
            } else {
                builder.append(c1.lowercaseChar())
                buf.reset()
            }
        } else {
            builder.append(c1.lowercaseChar())
        }
    }
    return builder.toString()
}

internal fun resolveLiteralTag(typeName: String): String {
    return when (typeName) {
        "kotlin.Long" -> "L"
        "kotlin.UInt" -> "u"
        else -> ""
    }
}

internal fun resolveTypeArgumentsOfAncestor(descendantType: KSType, ancestorType: KSType): List<KSTypeArgument> {
    if (ancestorType.arguments.isEmpty() || !ancestorType.isAssignableFrom(descendantType)) return emptyList()
    val typeMap = descendantType.declaration.typeParameters.zip(descendantType.arguments).toMap()
    return resolveTypeArgumentsOfAncestor(descendantType, ancestorType, listOf(typeMap))
}

private fun resolveTypeArgumentsOfAncestor(descendantType: KSType, ancestorType: KSType, typeMapList: List<Map<KSTypeParameter, KSTypeArgument>> = emptyList()): List<KSTypeArgument> {
    return if (descendantType.declaration == ancestorType.declaration) {
        descendantType.declaration.typeParameters.zip(descendantType.arguments).asSequence().map {
            var formal: KSDeclaration? = it.first
            var actual: KSTypeArgument = it.second
            for (typeMap in typeMapList) {
                actual = typeMap[formal] ?: break
                formal = actual.type?.resolve()?.declaration
            }
            actual
        }.toList()
    } else {
        val descendantDecl = descendantType.declaration as KSClassDeclaration
        for (superTypeRef in descendantDecl.superTypes) {
            val superType = superTypeRef.resolve()
            if (!ancestorType.isAssignableFrom(superType)) continue
            val typeMap = superType.declaration.typeParameters.zip(superType.arguments).toMap()
            val newTypeMapList = listOf(typeMap) + typeMapList
            val typeArgs = resolveTypeArgumentsOfAncestor(superType, ancestorType, newTypeMapList)
            if (typeArgs.isNotEmpty()) return typeArgs
        }
        return emptyList()
    }
}
