package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Json
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import kotlin.reflect.typeOf

object PostgreSqlR2dbcJsonType : AbstractR2dbcDataType<Json>(typeOf<Json>()) {
    override val name: String = "json"

    override fun getValue(row: Row, index: Int): Json? {
        return row.get(index, Json::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Json? {
        return row.get(columnLabel, Json::class.java)
    }
}
