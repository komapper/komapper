package org.komapper.core

import java.nio.CharBuffer

interface NamingStrategy {

    fun apply(name: String): String
}

object CamelToLowerSnakeCase : NamingStrategy {

    private val camelToSnakeCase = CamelToSnakeCase(Char::lowercaseChar)

    override fun apply(name: String): String {
        return camelToSnakeCase.apply(name)
    }
}

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

object Implicit : NamingStrategy {
    override fun apply(name: String): String {
        return name
    }
}

object SnakeToLowerCamelCase : NamingStrategy {

    private val snakeToCamelCase = SnakeToCamelCase(Char::lowercaseChar)

    override fun apply(name: String): String {
        return snakeToCamelCase.apply(name)
    }
}

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
