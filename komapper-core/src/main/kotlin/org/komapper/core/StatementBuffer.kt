package org.komapper.core

import kotlin.reflect.KClass

class StatementBuffer(
    val format: (Any?, KClass<*>) -> String,
    capacity: Int = 200
) : Appendable {
    val sql = StringBuilder(capacity)
    val sqlWithArgs = StringBuilder(capacity)
    val values = ArrayList<Value>()

    override fun append(s: CharSequence): StatementBuffer {
        sql.append(s)
        sqlWithArgs.append(s)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
        sql.append(csq, start, end)
        sqlWithArgs.append(csq, start, end)
        return this
    }

    override fun append(c: Char): java.lang.Appendable {
        sql.append(c)
        sqlWithArgs.append(c)
        return this
    }

    fun append(i: Int): java.lang.Appendable {
        val s = i.toString()
        sql.append(s)
        sqlWithArgs.append(s)
        return this
    }

    fun append(statement: Statement): StatementBuffer {
        sql.append(statement.sql)
        values.addAll(statement.values)
        sqlWithArgs.append(statement.sqlWithArgs)
        return this
    }

    fun bind(value: Value): StatementBuffer {
        sql.append("?")
        sqlWithArgs.append(format(value.any, value.klass))
        values.add(value)
        return this
    }

    fun cutBack(length: Int): StatementBuffer {
        sql.setLength(sql.length - length)
        sqlWithArgs.setLength(sqlWithArgs.length - length)
        return this
    }

    fun toStatement(): Statement {
        if (sql.isEmpty() && values.isEmpty() && sqlWithArgs.isEmpty()) {
            return Statement.EMPTY
        }
        return Statement(sql.toString(), values, sqlWithArgs.toString())
    }

    override fun toString() = sql.toString()
}
