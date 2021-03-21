package org.komapper.core.data

data class Statement(val sql: String, val values: List<Value>, val log: String?) {
    constructor(sql: String) : this(sql, emptyList(), sql)
    override fun toString(): String {
        return sql
    }
}
