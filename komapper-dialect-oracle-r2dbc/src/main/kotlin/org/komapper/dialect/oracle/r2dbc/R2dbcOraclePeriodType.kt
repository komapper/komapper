package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.Row
import org.komapper.r2dbc.R2dbcAbstractType
import java.time.Period

object R2dbcOraclePeriodType : R2dbcAbstractType<Period>(Period::class) {
    override val name: String = "interval year to month"

    override fun getValue(row: Row, index: Int): Period? {
        return row.get(index, klass.java)
    }

    override fun getValue(row: Row, columnLabel: String): Period? {
        return row.get(columnLabel, klass.java)
    }
}
