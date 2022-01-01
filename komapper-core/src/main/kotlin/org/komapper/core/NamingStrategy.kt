package org.komapper.core

import java.nio.CharBuffer

/**
 * The naming strategy.
 */
@ThreadSafe
interface NamingStrategy {

    /**
     * Applies the naming strategy to the given [name].
     * @param name the name
     * @return the converted name
     */
    fun apply(name: String): String
}

/**
 * Converts the camel case name to the lower snake case name.
 */
object CamelToLowerSnakeCase : NamingStrategy {

    private val camelToSnakeCase = CamelToSnakeCase(Char::lowercaseChar)

    override fun apply(name: String): String {
        return camelToSnakeCase.apply(name)
    }
}

/**
 * Converts the camel case name to the upper snake case name.
 */
object CamelToUpperSnakeCase : NamingStrategy {

    private val camelToSnakeCase = CamelToSnakeCase(Char::uppercaseChar)

    override fun apply(name: String): String {
        return camelToSnakeCase.apply(name)
    }
}

private class CamelToSnakeCase(private val mapper: (Char) -> Char) {

    fun apply(name: String): String {
        val builder = StringBuilder()
        val buf = CharBuffer.wrap(name)
        while (buf.hasRemaining()) {
            val c1 = buf.get()
            builder.append(mapper(c1))
            buf.mark()
            if (buf.hasRemaining()) {
                val c2 = buf.get()
                if (!c1.isUpperCase() && c2.isUpperCase()) {
                    builder.append("_")
                }
                buf.reset()
            }
        }
        return builder.toString()
    }
}

/**
 * Converts nothing.
 */
object Implicit : NamingStrategy {
    override fun apply(name: String): String {
        return name
    }
}

/**
 * Converts the snake case name to the lower camel case name.
 */
object SnakeToLowerCamelCase : NamingStrategy {

    private val snakeToCamelCase = SnakeToCamelCase(Char::lowercaseChar)

    override fun apply(name: String): String {
        return snakeToCamelCase.apply(name)
    }
}

/**
 * Converts the snake case name to the upper camel case name.
 */
object SnakeToUpperCamelCase : NamingStrategy {

    private val snakeToCamelCase = SnakeToCamelCase(Char::uppercaseChar)

    override fun apply(name: String): String {
        return snakeToCamelCase.apply(name)
    }
}

private class SnakeToCamelCase(private val mapper: (Char) -> Char) : NamingStrategy {

    override fun apply(name: String): String {
        if (name.isBlank()) {
            return name
        }
        val list = name.split("_")
        val result = StringBuilder()
        result.append(list.first().lowercase().replaceFirstChar(mapper))
        for (remaining in list.subList(1, list.size)) {
            result.append(remaining.lowercase().replaceFirstChar(Char::uppercase))
        }
        return result.toString()
    }
}
