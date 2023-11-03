package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import org.locationtech.jts.geom.Geometry

object PostgreSqlR2dbcGeometryType : AbstractR2dbcDataType<Geometry>(Geometry::class) {
    override val name: String = "geometry"

    override fun getValue(row: Row, index: Int): Geometry? {
        return row.get(index, Geometry::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Geometry? {
        return row.get(columnLabel, Geometry::class.java)
    }

    override fun doToString(value: Geometry): String {
        return "'$value'"
    }
}
