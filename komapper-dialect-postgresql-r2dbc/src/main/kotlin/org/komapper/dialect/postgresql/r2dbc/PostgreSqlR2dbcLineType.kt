package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Line
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType

object PostgreSqlR2dbcLineType : AbstractR2dbcDataType<Line>(Line::class) {
    override val name: String = "line"

    override fun getValue(row: Row, index: Int): Line? {
        return row.get(index, Line::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Line? {
        return row.get(columnLabel, Line::class.java)
    }
}
