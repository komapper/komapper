package org.komapper.jdbc

import org.komapper.core.Dialect
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

/**
 * Represents a dialect for JDBC access.
 */
interface JdbcDialect : Dialect {
    /**
     * Data types.
     */
    val dataTypes: List<JdbcDataType<*>>

    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param index the column index
     * @param valueClass the value class
     * @return the value
     */
    fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any?

    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param columnLabel the column label
     * @param valueClass the value class
     * @return the value
     */
    fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any?

    /**
     * Sets the value.
     *
     * @param ps the prepared statement
     * @param index the column index
     * @param value the value
     * @param valueClass the value class
     */
    fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>)

    /**
     * Returns the data type.
     *
     * @param klass the value class
     * @return the data type
     */
    fun getDataType(klass: KClass<*>): JdbcDataType<*>

    /**
     * Returns whether the exception indicates that the sequence already exists.
     */
    fun isSequenceExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates that the sequence does not exist.
     */
    fun isSequenceNotExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates that the table already exists.
     */
    fun isTableExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates that the table does not exist.
     */
    fun isTableNotExistsError(exception: SQLException): Boolean = false

    /**
     * Returns whether the exception indicates unique constraint violation.
     *
     * @param exception the exception
     * @return whether the exception indicates unique constraint violation
     */
    fun isUniqueConstraintViolationError(exception: SQLException): Boolean

/**
     * Returns whether the [java.sql.Statement.RETURN_GENERATED_KEYS] flag is supported.
     *
     * @return whether the RETURN_GENERATED_KEYS flat is supported
     */
    fun supportsReturnGeneratedKeysFlag(): Boolean = true
}

abstract class JdbcAbstractDialect protected constructor(internalDataTypes: List<JdbcDataType<*>> = emptyList()) :
    JdbcDialect {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dataTypeMap: Map<KClass<*>, JdbcDataType<*>> = internalDataTypes.associateBy { it.klass }
    override val dataTypes = internalDataTypes

    override fun getValue(rs: ResultSet, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, index)
    }

    override fun getValue(rs: ResultSet, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(rs, columnLabel)
    }

    override fun setValue(ps: PreparedStatement, index: Int, value: Any?, valueClass: KClass<*>) {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as JdbcDataType<Any>
        dataType.setValue(ps, index, value)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>, masking: Boolean): String {
        return if (masking) {
            mask
        } else {
            val dataType = getDataType(valueClass)
            @Suppress("UNCHECKED_CAST")
            dataType as JdbcDataType<Any>
            dataType.toString(value)
        }
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }

    override fun getDataType(klass: KClass<*>): JdbcDataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }
}
