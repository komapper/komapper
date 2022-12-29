package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.runner.ValueExtractor
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet

internal class JdbcValueExtractor(private val dataOperator: JdbcDataOperator, private val resultSet: ResultSet) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        return ValueExtractor.execute(expression, index) {
            dataOperator.getValue(resultSet, ++index, expression.interiorClass)
        }
    }
}
