package org.komapper.core.dsl.query

import org.komapper.core.JdbcDialect
import org.komapper.core.dsl.expression.ColumnExpression
import java.sql.ResultSet
import kotlin.reflect.cast

internal class PropertyMapper(val dialect: JdbcDialect, private val resultSet: ResultSet) {
    private var index = 0

    fun <EXTERIOR : Any, INTERIOR : Any> execute(expression: ColumnExpression<EXTERIOR, INTERIOR>): EXTERIOR? {
        val value = dialect.getValue(resultSet, ++index, expression.interiorClass)
        return if (value == null) {
            null
        } else {
            val interior = expression.interiorClass.cast(value)
            val exterior = expression.wrap(interior)
            exterior
        }
    }
}
