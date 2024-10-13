package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.runner.ValueExtractor
import org.komapper.r2dbc.R2dbcDataOperator

internal interface R2dbcValueExtractor {
    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR?
}

internal class R2dbcIndexedValueExtractor(private val dataOperator: R2dbcDataOperator, private val row: Row) : R2dbcValueExtractor {
    private var index = 0

    override fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        return ValueExtractor.getByIndex(expression, index) {
            dataOperator.getValue(row, index++, expression.interiorType)
        }
    }
}

internal class R2dbcNamedValueExtractor(private val dataOperator: R2dbcDataOperator, private val row: Row) : R2dbcValueExtractor {
    override fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        return ValueExtractor.getByName(expression) {
            dataOperator.getValue(row, expression.columnName, expression.interiorType)
        }
    }
}
