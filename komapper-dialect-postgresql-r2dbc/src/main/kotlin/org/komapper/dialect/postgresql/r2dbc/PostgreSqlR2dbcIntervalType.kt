package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Interval
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import kotlin.reflect.typeOf

object PostgreSqlR2dbcIntervalType : AbstractR2dbcDataType<Interval>(typeOf<Interval>()) {
    override val name: String = "interval"

    override fun getValue(row: Row, index: Int): Interval? {
        return row.get(index, Interval::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Interval? {
        return row.get(columnLabel, Interval::class.java)
    }
}
