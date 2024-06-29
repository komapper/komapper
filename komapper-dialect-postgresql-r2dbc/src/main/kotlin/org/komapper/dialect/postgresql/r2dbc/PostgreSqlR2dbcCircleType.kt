package org.komapper.dialect.postgresql.r2dbc

import io.r2dbc.postgresql.codec.Circle
import io.r2dbc.spi.Row
import org.komapper.r2dbc.AbstractR2dbcDataType
import kotlin.reflect.typeOf

object PostgreSqlR2dbcCircleType : AbstractR2dbcDataType<Circle>(typeOf<Circle>()) {
    override val name: String = "circle"

    override fun getValue(row: Row, index: Int): Circle? {
        return row.get(index, Circle::class.java)
    }

    override fun getValue(row: Row, columnLabel: String): Circle? {
        return row.get(columnLabel, Circle::class.java)
    }
}
