package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.Statement
import org.komapper.r2dbc.R2dbcAbstractDataType

object R2dbcOracleBooleanType : R2dbcAbstractDataType<Boolean>(Boolean::class) {
    override val name: String = "integer(1)"

    override fun convert(value: Any): Boolean {
        return when (value) {
            is Number -> value.toInt() == 1
            else -> false
        }
    }

    override fun bind(statement: Statement, index: Int, value: Boolean) {
        statement.bind(index, toInt(value))
    }

    override fun bind(statement: Statement, name: String, value: Boolean) {
        statement.bind(name, toInt(value))
    }

    override fun doToString(value: Boolean): String {
        return toInt(value).toString()
    }

    private fun toInt(value: Boolean): Int {
        return if (value) 1 else 0
    }
}