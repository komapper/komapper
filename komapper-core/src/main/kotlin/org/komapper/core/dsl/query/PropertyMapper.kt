package org.komapper.core.dsl.query

import org.komapper.core.config.Dialect
import org.komapper.core.dsl.expr.PropertyExpression
import java.sql.ResultSet
import kotlin.reflect.cast

internal class PropertyMapper(val dialect: Dialect, val resultSet: ResultSet) {
    private var index = 0

    fun <T : Any> execute(propertyExpression: PropertyExpression<T>): T {
        val value = dialect.getValue(resultSet, ++index, propertyExpression.klass)
        return propertyExpression.klass.cast(value)
    }
}
