package org.komapper.core.dsl.query

import org.komapper.core.Dialect
import org.komapper.core.dsl.expression.PropertyExpression
import java.sql.ResultSet
import kotlin.reflect.cast

internal class PropertyMapper(val dialect: Dialect, val resultSet: ResultSet) {
    private var index = 0

    fun <T : Any> execute(propertyExpression: PropertyExpression<T>): T? {
        val value = dialect.getValue(resultSet, ++index, propertyExpression.klass)
        return if (value == null) null else propertyExpression.klass.cast(value)
    }
}
