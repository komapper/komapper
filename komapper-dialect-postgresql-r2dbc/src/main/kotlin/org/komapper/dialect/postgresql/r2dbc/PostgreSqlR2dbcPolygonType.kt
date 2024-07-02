package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Polygon
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import kotlin.reflect.typeOf

object PostgreSqlR2dbcPolygonType : AbstractR2dbcDataType<Polygon>(typeOf<Polygon>()) {
    override val name: String = "polygon"

    override fun getValue(row: Row, index: Int): Polygon? {
        return row.get(index, Polygon::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Polygon? {
        return row.get(columnLabel, Polygon::class.java)
    }
}
