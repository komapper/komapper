package org.komapper.core

import kotlin.reflect.KClass

/**
 * The SQL statement.
 *
 * @property parts the parts of the SQL statement.
 */
@ThreadSafe
data class Statement(val parts: List<StatementPart>) {
    constructor(text: CharSequence) : this(listOf(StatementPart.Text(text)))

    companion object {
        val EMPTY = Statement(emptyList())

        fun createBindVariable(
            @Suppress("UNUSED_PARAMETER") index: Int,
            value: StatementPart.Value
        ): CharSequence {
            return value
        }
    }

    /**
     * The arguments of the SQL statement.
     */
    val args: List<Value> = parts.filterIsInstance<StatementPart.Value>().map { it.value }

    /**
     * Converts the SQL statement to an SQL string.
     *
     * @param format the format function of the bind values
     */
    fun toSql(format: (Int, StatementPart.Value) -> CharSequence = ::createBindVariable): String {
        var index = 0
        return parts.joinToString(separator = "") { part ->
            when (part) {
                is StatementPart.Text -> part
                is StatementPart.Value -> {
                    format(index++, part)
                }
            }
        }
    }

    /**
     * Converts the SQL statement to an SQL string with arguments.
     *
     * @param format the format function of the bound values
     */
    fun toSqlWithArgs(format: (Any?, KClass<*>, Boolean) -> CharSequence): String {
        return parts.joinToString(separator = "") { part ->
            when (part) {
                is StatementPart.Text -> part.text
                is StatementPart.Value -> {
                    val value = part.value
                    format(value.any, value.klass, value.masking)
                }
            }
        }
    }

    /**
     * Composes the SQL statement.
     *
     * @param other the other SQL statement
     */
    infix operator fun plus(other: Statement): Statement {
        val separator =
            if (this.parts.isEmpty() || this.parts.last().trimEnd().endsWith(";")) "" else ";"
        val parts = this.parts + StatementPart.Text(separator) + other.parts
        return Statement(parts)
    }

    /**
     * Adds a part of the SQL statement.
     *
     * @param text a part of the SQL statement
     */
    infix operator fun plus(text: CharSequence): Statement {
        return copy(parts = parts + StatementPart.Text(text))
    }
}
