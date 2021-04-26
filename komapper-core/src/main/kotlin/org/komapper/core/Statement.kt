package org.komapper.core

data class Statement(val sql: String, val values: List<Value>, val sqlWithArgs: String) {
    constructor(sql: String) : this(sql, emptyList(), sql)

    companion object {
        val EMPTY = Statement("", emptyList(), "")
    }

    override fun toString(): String {
        return sql
    }

    infix operator fun plus(other: Statement): Statement {
        val separator = if (this.sql.trimEnd().endsWith(";")) "" else ";"
        val sql = this.sql + separator + other.sql
        val values = this.values + other.values
        val sqlWithArgs = this.sqlWithArgs + separator + other.sqlWithArgs
        return Statement(sql, values, sqlWithArgs)
    }
}
