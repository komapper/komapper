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
    return this.modifiers.contains(Modifier.VALUE)
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
                val typeParameter = typeArgument.type?.resolve()?.declaration?.accept(TypeParameterVisitor(), Unit)
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

private fun createBackquotedName(declaration: KSDeclaration): String {
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

internal val EntityDefinitionSource.names: List<String>
    get() = this.aliases.ifEmpty {
        val alias = toCamelCase(this.entityDeclaration.simpleName.asString())
        listOf(alias)
    }

internal val EntityDefinitionSource.typeName get() = createBackquotedName(entityDeclaration)

internal fun EntityDefinitionSource.createUnitTypeName(config: Config) =
    this.unitDeclaration?.qualifiedName?.asString() ?: config.metaObject

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
