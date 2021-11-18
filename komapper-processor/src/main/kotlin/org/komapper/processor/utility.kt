package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import java.nio.CharBuffer
import kotlin.reflect.KClass

internal fun <T> Sequence<T>.anyDuplicates(predicate: (T) -> Boolean): Boolean {
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
