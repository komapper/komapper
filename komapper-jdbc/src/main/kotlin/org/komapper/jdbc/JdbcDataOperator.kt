package org.komapper.jdbc

import org.komapper.core.DataOperator
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KType

interface JdbcDataOperator : DataOperator {
    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param index the column index
     * @param type the value type
     * @return the value
     */
    fun <T : Any> getValue(rs: ResultSet, index: Int, type: KType): T?

    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param columnLabel the column label
     * @param type the value type
     * @return the value
     */
    fun <T : Any> getValue(rs: ResultSet, columnLabel: String, type: KType): T?

    /**
     * Sets the value.
     *
     * @param ps the prepared statement
     * @param index the column index
     * @param value the value
     * @param type the value type
     */
    fun <T : Any> setValue(ps: PreparedStatement, index: Int, value: T?, type: KType)

    /**
     * Registers the return parameter.
     *
     * @param ps the prepared statement
     * @param index the column index
     * @param type the parameter type
     */
    fun <T : Any> registerReturnParameter(ps: PreparedStatement, index: Int, type: KType)

    /**
     * Returns the data type.
     *
     * @param type the value type
     * @return the data type
     */
    fun <T : Any> getDataType(type: KType): JdbcDataType<*>

    /**
     * Returns the data type or null.
     *
     * @param type the value type
     * @return the data type or null
     */
    fun <T : Any> getDataTypeOrNull(type: KType): JdbcDataType<T>?
}

class DefaultJdbcDataOperator(private val dialect: JdbcDialect, private val dataTypeProvider: JdbcDataTypeProvider) :
    JdbcDataOperator {
    override fun <T : Any> getValue(rs: ResultSet, index: Int, type: KType): T? {
        val dataType = getDataType<T>(type)
        return dataType.getValue(rs, index)
    }

    override fun <T : Any> getValue(rs: ResultSet, columnLabel: String, type: KType): T? {
        val dataType = getDataType<T>(type)
        return dataType.getValue(rs, columnLabel)
    }

    override fun <T : Any> setValue(ps: PreparedStatement, index: Int, value: T?, type: KType) {
        val dataType = getDataType<T>(type)
        dataType.setValue(ps, index, value)
    }

    override fun <T : Any> registerReturnParameter(ps: PreparedStatement, index: Int, type: KType) {
        val dataType = getDataType<T>(type)
        dataType.registerReturnParameter(ps, index)
    }

    override fun <T : Any> formatValue(value: T?, type: KType, masking: Boolean): String {
        return if (masking) {
            dialect.mask
        } else {
            val dataType = getDataTypeOrNull<T>(type)
            dataType?.toString(value) ?: value.toString()
        }
    }

    override fun <T : Any> getDataTypeName(type: KType): String {
        val dataType = getDataType<T>(type)
        return dataType.name
    }

    override fun <T : Any> getDataType(type: KType): JdbcDataType<T> {
        return getDataTypeOrNull(type) ?: error(
            "The dataType is not found for the type \"${type}\".",
        )
    }

    override fun <T : Any> getDataTypeOrNull(type: KType): JdbcDataType<T>? {
        return dataTypeProvider.get(type)
    }
}
