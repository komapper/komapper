package org.komapper.jdbc

import org.komapper.core.DataOperator
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

interface JdbcDataOperator : DataOperator {
    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param index the column index
     * @param valueClass the value class
     * @return the value
     */
    fun <T : Any> getValue(rs: ResultSet, index: Int, valueClass: KClass<out T>): T?

    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param columnLabel the column label
     * @param valueClass the value class
     * @return the value
     */
    fun <T : Any> getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<out T>): T?

    /**
     * Sets the value.
     *
     * @param ps the prepared statement
     * @param index the column index
     * @param value the value
     * @param valueClass the value class
     */
    fun <T : Any> setValue(ps: PreparedStatement, index: Int, value: T?, valueClass: KClass<out T>)

    /**
     * Returns the data type.
     *
     * @param klass the value class
     * @return the data type
     */
    fun <T : Any> getDataType(klass: KClass<out T>): JdbcDataType<T>
}

class DefaultJdbcDataOperator(private val dialect: JdbcDialect, private val dataTypeProvider: JdbcDataTypeProvider) :
    JdbcDataOperator {
    override fun <T : Any> getValue(rs: ResultSet, index: Int, valueClass: KClass<out T>): T? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun <T : Any> getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<out T>): T? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, columnLabel)
    }

    override fun <T : Any> setValue(ps: PreparedStatement, index: Int, value: T?, valueClass: KClass<out T>) {
        val dataType = getDataType(valueClass)
        dataType.setValue(ps, index, value)
    }

    override fun <T : Any> formatValue(value: T?, valueClass: KClass<out T>, masking: Boolean): String {
        return if (masking) {
            dialect.mask
        } else {
            val dataType = getDataType(valueClass)
            dataType.toString(value)
        }
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }

    override fun <T : Any> getDataType(klass: KClass<out T>): JdbcDataType<T> {
        return dataTypeProvider.get(klass) ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\".",
        )
    }
}
