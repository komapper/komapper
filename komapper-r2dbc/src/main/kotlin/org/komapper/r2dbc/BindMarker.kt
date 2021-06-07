package org.komapper.r2dbc

import io.r2dbc.spi.Statement
import org.komapper.core.PlaceHolder

interface BindMarker {
    fun applyMarkers(statement: org.komapper.core.Statement): org.komapper.core.Statement
    fun setValue(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>)
}

object DefaultBindMarker : BindMarker {
    override fun applyMarkers(statement: org.komapper.core.Statement): org.komapper.core.Statement {
        return statement
    }

    override fun setValue(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>) {
        dataType.setValue(statement, index, value)
    }
}

object IndexedBindMarker : BindMarker {
    override fun applyMarkers(statement: org.komapper.core.Statement): org.komapper.core.Statement {
        var index = 0
        return statement.sql.map {
            when (it) {
                is PlaceHolder -> "$${++index}"
                else -> it
            }
        }.let { statement.copy(sql = it) }
    }

    override fun setValue(statement: Statement, index: Int, value: Any?, dataType: R2dbcDataType<Any>) {
        dataType.setValue(statement, "$${index + 1}", value)
    }
}
