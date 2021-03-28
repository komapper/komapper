package org.komapper.core.dsl.query

import org.komapper.core.config.Dialect
import org.komapper.core.metamodel.ColumnInfo
import java.sql.ResultSet
import kotlin.reflect.cast

internal class PropertyMapper(val dialect: Dialect, val resultSet: ResultSet) {
    private var index = 0

    fun <T : Any> execute(c: ColumnInfo<T>): T {
        val value = dialect.getValue(resultSet, ++index, c.klass)
        return c.klass.cast(value)
    }
}
