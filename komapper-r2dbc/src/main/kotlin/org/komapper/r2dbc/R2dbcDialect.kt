package org.komapper.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.Dialect
import org.komapper.core.StatementPart
import kotlin.reflect.KClass

/**
 * Represents a dialect for R2DBC access.
 */
interface R2dbcDialect : Dialect {
    /**
     * Data types.
     */
    val dataTypes: List<R2dbcDataType<*>>

    /**
     * Returns the value.
     * @param row the row
     * @param index the index
     * @param valueClass the value class
     * @return the value
     */
    fun getValue(row: Row, index: Int, valueClass: KClass<*>): Any?

    /**
     * Returns the value.
     * @param row the row
     * @param columnLabel the column label
     * @param valueClass the value class
     * @return the value
     */
    fun getValue(row: Row, columnLabel: String, valueClass: KClass<*>): Any?

    /**
     * Sets the value.
     * @param statement the statement
     * @param index the index
     * @param value the value
     * @param valueClass the value class
     */
    fun setValue(statement: Statement, index: Int, value: Any?, valueClass: KClass<*>)

    /**
     * Returns the data type.
     * @param klass the value class
     * @return the data type
     */
    fun getDataType(klass: KClass<*>): R2dbcDataType<*>
}

abstract class AbstractR2dbcDialect protected constructor(internalDataTypes: List<R2dbcDataType<*>> = emptyList()) :
    R2dbcDialect {

    @Suppress("MemberVisibilityCanBePrivate")
    protected val dataTypeMap: Map<KClass<*>, R2dbcDataType<*>> = internalDataTypes.associateBy { it.klass }
    override val dataTypes = internalDataTypes

    protected open fun getBinder(): Binder {
        return DefaultBinder
    }

    override fun replacePlaceHolder(index: Int, placeHolder: StatementPart.PlaceHolder): CharSequence {
        val binder = getBinder()
        return binder.replace(index, placeHolder)
    }

    override fun getValue(row: Row, index: Int, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, index)
    }

    override fun getValue(row: Row, columnLabel: String, valueClass: KClass<*>): Any? {
        val dataType = getDataType(valueClass)
        return dataType.getValue(row, columnLabel)
    }

    override fun setValue(statement: Statement, index: Int, value: Any?, valueClass: KClass<*>) {
        val dataType = getDataType(valueClass)
        @Suppress("UNCHECKED_CAST")
        dataType as R2dbcDataType<Any>
        val bindMarker = getBinder()
        return bindMarker.bind(statement, index, value, dataType)
    }

    override fun formatValue(value: Any?, valueClass: KClass<*>, masking: Boolean): String {
        return if (masking) {
            mask
        } else {
            val dataType = getDataType(valueClass)
            @Suppress("UNCHECKED_CAST")
            dataType as R2dbcDataType<Any>
            return dataType.toString(value)
        }
    }

    override fun getDataType(klass: KClass<*>): R2dbcDataType<*> {
        return dataTypeMap[klass] ?: error(
            "The dataType is not found for the type \"${klass.qualifiedName}\"."
        )
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        val dataType = getDataType(klass)
        return dataType.name
    }
}
