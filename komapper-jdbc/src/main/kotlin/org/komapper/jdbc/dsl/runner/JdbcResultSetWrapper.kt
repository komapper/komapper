package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.query.Row
import org.komapper.jdbc.JdbcDataOperator
import java.sql.ResultSet
import kotlin.reflect.KType

internal class JdbcResultSetWrapper(
    private val dataOperator: JdbcDataOperator,
    private val rs: ResultSet,
) : Row {

    override fun <T : Any> get(index: Int, type: KType): T? {
        return dataOperator.getValue(rs, index + 1, type)
    }

    override fun <T : Any> get(columnLabel: String, type: KType): T? {
        return dataOperator.getValue(rs, columnLabel, type)
    }
}
