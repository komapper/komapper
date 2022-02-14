package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.jdbc.JdbcDialect
import java.sql.ResultSet

internal class JdbcPropertyMapper(private val dialect: JdbcDialect, private val resultSet: ResultSet) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        val value = dialect.getValue(resultSet, ++index, expression.interiorClass)
        return if (value == null) null else expression.wrap(value)
    }
}
