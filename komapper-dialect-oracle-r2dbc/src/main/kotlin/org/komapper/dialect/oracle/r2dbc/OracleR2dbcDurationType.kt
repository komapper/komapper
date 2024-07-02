package org.komapper.dialect.oracle.r2dbc

import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import java.time.Duration
import kotlin.reflect.typeOf

object OracleR2dbcDurationType : AbstractR2dbcDataType<Duration>(typeOf<Duration>()) {
    override val name: String = "interval day to second"

    override fun getValue(row: Row, index: Int): Duration? {
        return row.get(index, Duration::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Duration? {
        return row.get(columnLabel, Duration::class.java)
    }
}
