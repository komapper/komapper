package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.runner.ValueExtractor
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet

internal interface JdbcValueExtractor {
    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR?
}

internal class JdbcIndexedValueExtractor(private val dataOperator: JdbcDataOperator, private val resultSet: ResultSet) : JdbcValueExtractor {
    private var index = 0

    override fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        return ValueExtractor.getByIndex(expression, index) {
            dataOperator.getValue(resultSet, ++index, expression.interiorType)
        }
    }
}

internal class JdbcNamedValueExtractor(private val dataOperator: JdbcDataOperator, private val resultSet: ResultSet) : JdbcValueExtractor {

    override fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        return ValueExtractor.getByName(expression) {
            dataOperator.getValue(resultSet, expression.columnName, expression.interiorType)
        }
    }
}
