package org.komapper.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.DataOperator
import org.komapper.core.DataType
import kotlin.reflect.KType

interface R2dbcDataOperator : DataOperator {
    /**
     * Returns the value.
     *
     * @param row the row
     * @param index the index
     * @param type the value type
     * @return the value
     */
    fun <T : Any> getValue(row: Row, index: Int, type: KType): T?

    /**
     * Returns the value.
     *
     * @param row the row
     * @param columnLabel the column label
     * @param type the value type
     * @return the value
     */
    fun <T : Any> getValue(row: Row, columnLabel: String, type: KType): T?

    /**
     * Sets the value.
     *
     * @param statement the statement
     * @param index the index
     * @param value the value
     * @param type the value type
     */
    fun <T : Any> setValue(statement: Statement, index: Int, value: T?, type: KType)
}

class DefaultR2dbcDataOperator(private val dialect: R2dbcDialect, private val dataTypeProvider: R2dbcDataTypeProvider) :
    R2dbcDataOperator {
    override fun <T : Any> getValue(row: Row, index: Int, type: KType): T? {
        val dataType = getR2dbcDataType<T>(type)
        return dataType.getValue(row, index)
    }

    override fun <T : Any> getValue(row: Row, columnLabel: String, type: KType): T? {
        val dataType = getR2dbcDataType<T>(type)
        return dataType.getValue(row, columnLabel)
    }

    override fun <T : Any> setValue(statement: Statement, index: Int, value: T?, type: KType) {
        val dataType = getR2dbcDataType<T>(type)
        val bindMarker = dialect.getBinder()
        return bindMarker.bind(statement, index, value, dataType)
    }

    override fun <T : Any> formatValue(value: T?, type: KType, masking: Boolean): String {
        return if (masking) {
            dialect.mask
        } else {
            dataTypeProvider.get<T>(type)?.toString(value) ?: value.toString()
        }
    }

    override fun getDataType(type: KType): DataType {
        return getR2dbcDataType<Any>(type)
    }

    private fun <T : Any> getR2dbcDataType(type: KType): R2dbcDataType<T> {
        return dataTypeProvider.get(type) ?: error(
            "The dataType is not found for the type \"${type}\".",
        )
    }

    override fun <T : Any> getDataTypeName(type: KType): String {
        return getDataType(type).name
    }
}
