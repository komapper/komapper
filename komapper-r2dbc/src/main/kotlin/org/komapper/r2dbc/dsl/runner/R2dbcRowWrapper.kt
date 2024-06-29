package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.query.Row
import org.komapper.r2dbc.R2dbcDataOperator
import kotlin.reflect.KType

internal class R2dbcRowWrapper(
    private val dataOperator: R2dbcDataOperator,
    private val row: io.r2dbc.spi.Row,
) : Row {

    override fun <T : Any> get(index: Int, type: KType): T? {
        return dataOperator.getValue(row, index, type)
    }

    override fun <T : Any> get(columnLabel: String, type: KType): T? {
        return dataOperator.getValue(row, columnLabel, type)
    }
}
