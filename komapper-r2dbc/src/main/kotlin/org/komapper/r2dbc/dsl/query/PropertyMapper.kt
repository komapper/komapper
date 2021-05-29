package org.komapper.r2dbc.dsl.query

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.r2dbc.R2dbcDialect
import kotlin.reflect.cast

internal class PropertyMapper(val dialect: R2dbcDialect, private val row: Row) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        val value = row.get(index++)
        return if (value == null) {
            null
        } else {
            val interior = expression.interiorClass.cast(value)
            val exterior = expression.wrap(interior)
            exterior
        }
    }
}
