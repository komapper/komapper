package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Interval
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType

object R2dbcPostgreSqlIntervalType : AbstractR2dbcDataType<Interval>(Interval::class) {
    override val name: String = "interval"

    override fun getValue(row: Row, index: Int): Interval? {
        return row.get(index, Interval::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Interval? {
        return row.get(columnLabel, Interval::class.java)
    }
}
