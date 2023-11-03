package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Box
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType

object PostgreSqlR2dbcBoxType : AbstractR2dbcDataType<Box>(Box::class) {
    override val name: String = "box"

    override fun getValue(row: Row, index: Int): Box? {
        return row.get(index, Box::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Box? {
        return row.get(columnLabel, Box::class.java)
    }
}
