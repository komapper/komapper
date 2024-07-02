package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Point
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import kotlin.reflect.typeOf

object PostgreSqlR2dbcPointType : AbstractR2dbcDataType<Point>(typeOf<Point>()) {
    override val name: String = "point"

    override fun getValue(row: Row, index: Int): Point? {
        return row.get(index, Point::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Point? {
        return row.get(columnLabel, Point::class.java)
    }
}
