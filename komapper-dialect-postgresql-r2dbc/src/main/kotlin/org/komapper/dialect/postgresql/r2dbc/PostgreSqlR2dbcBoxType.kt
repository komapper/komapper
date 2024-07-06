package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Box
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import kotlin.reflect.typeOf

object PostgreSqlR2dbcBoxType : AbstractR2dbcDataType<Box>(typeOf<Box>()) {
    override val name: String = "box"

    override fun getValue(row: Row, index: Int): Box? {
        return row.get(index, Box::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Box? {
        return row.get(columnLabel, Box::class.java)
    }
}
