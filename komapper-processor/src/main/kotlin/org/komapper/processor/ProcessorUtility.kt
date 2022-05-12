package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.Modifier
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

internal fun KSAnnotated.findAnnotation(klass: KClass<*>): KSAnnotation? {
    return this.annotations.firstOrNull { it.shortName.asString() == klass.simpleName }
}

internal fun KSAnnotated.findAnnotation(simpleName: String): KSAnnotation? {
    return this.annotations.firstOrNull { it.shortName.asString() == simpleName }
}

internal fun KSType.normalize(): KSType {
    return this.declaration.accept(
        object : KSEmptyVisitor<Unit, KSType>() {
            override fun defaultHandler(node: KSNode, data: Unit): KSType {
                return this@normalize
            }

            override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): KSType {
                return typeAlias.type.resolve().normalize()
            }
        },
        Unit
    )
}

internal fun KSType.buildQualifiedName(): String {
    val parentDeclaration = declaration.parentDeclaration
    val base = if (parentDeclaration == null) {
        declaration.packageName.asString()
    } else {
        (parentDeclaration.qualifiedName ?: parentDeclaration.simpleName).asString()
    }
    return "$base.$this"
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
