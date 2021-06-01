package org.komapper.r2dbc

import io.r2dbc.spi.Statement
import org.komapper.core.PlaceHolder

interface BindMarker {
    fun apply(sql: List<CharSequence>): List<CharSequence>

    fun setValue(statement: Statement, index: Int, value: Any?, dataType: DataType<Any>)
}

object DefaultBindMarker : BindMarker {
    override fun apply(sql: List<CharSequence>): List<CharSequence> {
        return sql
    }

    override fun setValue(statement: Statement, index: Int, value: Any?, dataType: DataType<Any>) {
        dataType.setValue(statement, index, value)
    }
}

object IndexedBindMarker : BindMarker {
    override fun apply(sql: List<CharSequence>): List<CharSequence> {
        var index = 0
        return sql.map {
            when (it) {
                is PlaceHolder -> "$${++index}"
                else -> it
            }
        }
    }

    override fun setValue(statement: Statement, index: Int, value: Any?, dataType: DataType<Any>) {
        dataType.setValue(statement, "$${index + 1}", value)
    }
}
