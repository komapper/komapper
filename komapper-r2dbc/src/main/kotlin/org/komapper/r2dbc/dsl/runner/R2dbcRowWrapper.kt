package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.query.Row
import org.komapper.r2dbc.R2dbcDataOperator
import kotlin.reflect.KClass
import kotlin.reflect.cast

internal class R2dbcRowWrapper(
    private val dataOperator: R2dbcDataOperator,
    private val row: io.r2dbc.spi.Row,
) : Row {

    override fun <T : Any> get(index: Int, klass: KClass<T>): T? {
        return dataOperator.getValue(row, index, klass)?.let { klass.cast(it) }
    }

    override fun <T : Any> get(columnLabel: String, klass: KClass<T>): T? {
        return dataOperator.getValue(row, columnLabel, klass)?.let { klass.cast(it) }
    }
}
