package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.Row
import org.komapper.r2dbc.R2dbcAbstractType

object R2dbcPostgreSqlIntervalType : R2dbcAbstractType<Json>(Json::class) {
    override val name: String = "interval"

    override fun getValue(row: Row, index: Int): Json? {
        return row.get(index, klass.java)
    }

    override fun getValue(row: Row, columnLabel: String): Json? {
        return row.get(columnLabel, klass.java)
    }
}
