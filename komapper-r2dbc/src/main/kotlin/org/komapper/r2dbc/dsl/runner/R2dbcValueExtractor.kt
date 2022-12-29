package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.runner.ValueExtractor
import org.komapper.r2dbc.R2dbcDataOperator

internal class R2dbcValueExtractor(private val dataOperator: R2dbcDataOperator, private val row: Row) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        return ValueExtractor.execute(expression, index) {
            dataOperator.getValue(row, index++, expression.interiorClass)
        }
    }
}
