package org.komapper.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.DataOperator
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

    /**
     * Returns the data type.
     *
     * @param type the value type
     * @return the data type
     */
    fun <T : Any> getDataType(type: KType): R2dbcDataType<T>

    /**
     * Returns the data type or null.
     *
     * @param type the value type
     * @return the data type or null
     */
    fun <T : Any> getDataTypeOrNull(type: KType): R2dbcDataType<T>?
}

class DefaultR2dbcDataOperator(private val dialect: R2dbcDialect, private val dataTypeProvider: R2dbcDataTypeProvider) :
    R2dbcDataOperator {
    override fun <T : Any> getValue(row: Row, index: Int, type: KType): T? {
        val dataType = getDataType<T>(type)
        return dataType.getValue(row, index)
    }

    override fun <T : Any> getValue(row: Row, columnLabel: String, type: KType): T? {
        val dataType = getDataType<T>(type)
        return dataType.getValue(row, columnLabel)
    }

    override fun <T : Any> setValue(statement: Statement, index: Int, value: T?, type: KType) {
        val dataType = getDataType<T>(type)
        val bindMarker = dialect.getBinder()
        return bindMarker.bind(statement, index, value, dataType)
    }

    override fun <T : Any> formatValue(value: T?, type: KType, masking: Boolean): String {
        return if (masking) {
            dialect.mask
        } else {
            val dataType = getDataTypeOrNull<T>(type)
            dataType?.toString(value) ?: value.toString()
        }
    }

    override fun <T : Any> getDataType(type: KType): R2dbcDataType<T> {
        return getDataTypeOrNull(type) ?: error(
            "The dataType is not found for the type \"${type}\".",
        )
    }

    override fun <T : Any> getDataTypeOrNull(type: KType): R2dbcDataType<T>? {
        return dataTypeProvider.get(type)
    }

    override fun <T : Any> getDataTypeName(type: KType): String {
        val dataType = getDataType<T>(type)
        return dataType.name
    }
}
