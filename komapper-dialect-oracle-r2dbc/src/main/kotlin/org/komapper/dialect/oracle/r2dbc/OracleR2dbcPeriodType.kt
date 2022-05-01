package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import java.time.Period

object OracleR2dbcPeriodType : AbstractR2dbcDataType<Period>(Period::class) {
    override val name: String = "interval year to month"

    override fun getValue(row: Row, index: Int): Period? {
        return row.get(index, Period::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Period? {
        return row.get(columnLabel, Period::class.java)
    }
}
