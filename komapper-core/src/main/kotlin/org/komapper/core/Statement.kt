package org.komapper.core

import kotlin.reflect.KClass

@ThreadSafe
data class Statement(val parts: List<StatementPart>) {
    constructor(text: CharSequence) : this(listOf(StatementPart.Text(text)))

    companion object {
        val EMPTY = Statement(emptyList())
    }

    val args: List<Value> = parts.filterIsInstance<StatementPart.PlaceHolder>().map { it.value }

    fun toSql(format: (Int, StatementPart.PlaceHolder) -> CharSequence = { _, placeHolder -> placeHolder }): String {
        var index = 0
        return parts.joinToString(separator = "") { part ->
            when (part) {
                is StatementPart.Text -> part
                is StatementPart.PlaceHolder -> {
                    format(index++, part)
                }
            }
        }
    }

    fun toSqlWithArgs(format: (Any?, KClass<*>) -> CharSequence): String {
        return parts.joinToString(separator = "") { part ->
            when (part) {
                is StatementPart.Text -> part.text
                is StatementPart.PlaceHolder -> {
                    val value = part.value
                    format(value.any, value.klass)
                }
            }
        }
    }

    infix operator fun plus(other: Statement): Statement {
        val separator =
            if (this.parts.isEmpty() || this.parts.last().trimEnd().endsWith(";")) "" else ";"
        val parts = this.parts + StatementPart.Text(separator) + other.parts
        return Statement(parts)
    }

    infix operator fun plus(text: CharSequence): Statement {
        return copy(parts = parts + StatementPart.Text(text))
    }
}
