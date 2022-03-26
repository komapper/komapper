package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import java.time.Duration

object R2dbcOracleDurationType : AbstractR2dbcDataType<Duration>(Duration::class) {
    override val name: String = "interval day to second"

    override fun getValue(row: Row, index: Int): Duration? {
        return row.get(index, klass.java)
    }

    override fun getValue(row: Row, columnLabel: String): Duration? {
        return row.get(columnLabel, klass.java)
    }
}
