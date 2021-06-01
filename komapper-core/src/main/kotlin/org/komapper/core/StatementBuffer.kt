package org.komapper.core

import kotlin.reflect.KClass

class StatementBuffer(
    val format: (Any?, KClass<*>) -> String,
    capacity: Int = 200
) {
    val sql = mutableListOf<CharSequence>()
    val sqlWithArgs = StringBuilder(capacity)
    val values = ArrayList<Value>()

    fun append(s: CharSequence): StatementBuffer {
        sql.add(s)
        sqlWithArgs.append(s)
        return this
    }

    fun append(statement: Statement): StatementBuffer {
        sql.addAll(statement.sql)
        values.addAll(statement.values)
        sqlWithArgs.append(statement.sqlWithArgs)
        return this
    }

    fun bind(value: Value): StatementBuffer {
        sql.add(PlaceHolder)
        sqlWithArgs.append(format(value.any, value.klass))
        values.add(value)
        return this
    }

    fun cutBack(length: Int): StatementBuffer {
        val last = sql.removeLast()
        if (last is PlaceHolder || last.length < length) error("Cannot cutBack.")
        val newLast = last.dropLast(length)
        if (newLast.isNotEmpty()) sql.add(newLast)
        sqlWithArgs.setLength(sqlWithArgs.length - length)
        return this
    }

    fun toStatement(): Statement {
        if (sql.isEmpty() && values.isEmpty() && sqlWithArgs.isEmpty()) {
            return Statement.EMPTY
        }
        return Statement(sql, values, sqlWithArgs.toString())
    }

    override fun toString() = sql.joinToString(separator = "")
}
