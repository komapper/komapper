package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.query.Row
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.cast

internal class JdbcResultSetWrapper(
    private val dataOperator: JdbcDataOperator,
    private val rs: ResultSet,
) : Row {

    override fun <T : Any> get(index: Int, klass: KClass<T>): T? {
        return dataOperator.getValue(rs, index + 1, klass)?.let { klass.cast(it) }
    }

    override fun <T : Any> get(columnLabel: String, klass: KClass<T>): T? {
        return dataOperator.getValue(rs, columnLabel, klass)?.let { klass.cast(it) }
    }
}
