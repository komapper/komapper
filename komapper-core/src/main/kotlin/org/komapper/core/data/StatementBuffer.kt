package org.komapper.core.data

import kotlin.reflect.KClass

class StatementBuffer(
    val formatter: (Any?, KClass<*>) -> String,
    capacity: Int = 200
) : Appendable {
    val sql = StringBuilder(capacity)
    val log = StringBuilder(capacity)
    val values = ArrayList<Value>()

    override fun append(s: CharSequence): StatementBuffer {
        sql.append(s)
        log.append(s)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): java.lang.Appendable {
        sql.append(csq, start, end)
        log.append(csq, start, end)
        return this
    }

    override fun append(c: Char): java.lang.Appendable {
        sql.append(c)
        log.append(c)
        return this
    }

    fun append(i: Int): java.lang.Appendable {
        val s = i.toString()
        sql.append(s)
        log.append(s)
        return this
    }

    fun append(statement: Statement): StatementBuffer {
        sql.append(statement.sql)
        values.addAll(statement.values)
        log.append(statement.log)
        return this
    }

    fun bind(value: Value): StatementBuffer {
        sql.append("?")
        log.append(formatter(value.any, value.klass))
        values.add(value)
        return this
    }

    fun cutBack(length: Int): StatementBuffer {
        sql.setLength(sql.length - length)
        log.setLength(log.length - length)
        return this
    }

    fun toStatement() = Statement(sql.toString(), values, log.toString())

    override fun toString() = sql.toString()
}
