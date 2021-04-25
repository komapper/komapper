package org.komapper.core.dsl.query

import org.komapper.core.Dialect
import org.komapper.core.dsl.expression.ColumnExpression
import java.sql.ResultSet
import kotlin.reflect.cast

internal class PropertyMapper(val dialect: Dialect, private val resultSet: ResultSet) {
    private var index = 0

    fun <T : Any> execute(expression: ColumnExpression<T>): T? {
        val value = dialect.getValue(resultSet, ++index, expression.klass)
        return if (value == null) null else expression.klass.cast(value)
    }
}
