package org.komapper.core

@ThreadSafe
data class Statement(val sql: List<CharSequence>, val values: List<Value>, val sqlWithArgs: String) {
    constructor(sql: CharSequence) : this(listOf(sql), emptyList(), sql.toString())

    companion object {
        val EMPTY = Statement(emptyList(), emptyList(), "")
    }

    override fun toString(): String {
        return sql.joinToString(separator = "")
    }

    infix operator fun plus(other: Statement): Statement {
        val separator = if (this.sql.isEmpty() || this.sql.last().trimEnd().endsWith(";")) "" else ";"
        val sql = this.sql + listOf(separator) + other.sql
        val values = this.values + other.values
        val sqlWithArgs = this.sqlWithArgs + separator + other.sqlWithArgs
        return Statement(sql, values, sqlWithArgs)
    }
}
