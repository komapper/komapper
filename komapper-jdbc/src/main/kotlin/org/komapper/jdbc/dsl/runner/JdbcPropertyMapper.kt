package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet

internal class JdbcPropertyMapper(private val dataOperator: JdbcDataOperator, private val resultSet: ResultSet) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        val value = dataOperator.getValue(resultSet, ++index, expression.interiorClass)
        return if (value == null) null else expression.wrap(value)
    }
}
