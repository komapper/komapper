package org.komapper.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.DataOperator
import kotlin.reflect.KClass

interface R2dbcDataOperator : DataOperator {

    /**
     * Returns the value.
     *
     * @param row the row
     * @param index the index
     * @param valueClass the value class
     * @return the value
     */
    fun <T : Any> getValue(row: Row, index: Int, valueClass: KClass<out T>): T?

    /**
     * Returns the value.
     *
     * @param row the row
     * @param columnLabel the column label
     * @param valueClass the value class
     * @return the value
     */
    fun <T : Any> getValue(row: Row, columnLabel: String, valueClass: KClass<out T>): T?

    /**
     * Sets the value.
     *
     * @param statement the statement
     * @param index the index
     * @param value the value
     * @param valueClass the value class
     */
    fun <T : Any> setValue(statement: Statement, index: Int, value: T?, valueClass: KClass<out T>)

    /**
     * Returns the data type.
     *
     * @param klass the value class
     * @return the data type
     */
    fun <T : Any> getDataType(klass: KClass<out T>): R2dbcDataType<T>
}

class DefaultR2dbcDataOperator(private val dialect: R2dbcDialect, private val dataTypeProvider: R2dbcDataTypeProvider) :
    R2dbcDataOperator {

    override fun <T : Any> getValue(row: Row, index: Int, valueClass: KClass<out T>): T? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, index)
    }

    override fun <T : Any> getValue(row: Row, columnLabel: String, valueClass: KClass<out T>): T? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, columnLabel)
    }

    override fun <T : Any> setValue(statement: Statement, index: Int, value: T?, valueClass: KClass<out T>) {
        val dataType = getDataType(valueClass)
        val bindMarker = dialect.getBinder()
        return bindMarker.bind(statement, index, value, dataType)
    }

    override fun <T : Any> formatValue(value: T?, valueClass: KClass<out T>, masking: Boolean): String {
        return if (masking) {
            dialect.mask
        } else {
            val dataType = getDataType(valueClass)
            return dataType.toString(value)
        }
    }

    override fun <T : Any> getDataType(klass: KClass<out T>): R2dbcDataType<T> {
        return dataTypeProvider.get(klass) ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }
}
