package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.runner.ValueExtractingException
import org.komapper.r2dbc.R2dbcDataOperator

internal class R2dbcPropertyMapper(private val dataOperator: R2dbcDataOperator, private val row: Row) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        val i = index
        return try {
            val value = dataOperator.getValue(row, index++, expression.interiorClass)
            if (value == null) null else expression.wrap(value)
        } catch (e: Exception) {
            throw ValueExtractingException(i, e)
        }
    }
}
