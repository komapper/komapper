package org.komapper.r2dbc.spi

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import org.komapper.core.SqlType
import org.komapper.core.ThreadSafe

/**
 * Represents a user-defined data type for R2DBC access.
 */
@ThreadSafe
interface R2dbcUserDefinedDataType<T : Any> : SqlType {
    /**
     * The R2DBC codec type.
     * The type must be a nullable type.
     */
    val r2dbcType: Class<*>

    /**
     * Returns the value.
     *
     * @param row the row
     * @param index the index
     * @return the value
     */
    fun getValue(row: Row, index: Int): T?

    /**
     * Returns the value.
     *
     * @param row the row
     * @param columnLabel the column label
     * @return the value
     */
    fun getValue(row: Row, columnLabel: String): T?

    /**
     * Sets the value.
     *
     * @param statement the statement
     * @param index the index
     * @param value the value
     */
    fun setValue(statement: Statement, index: Int, value: T)

    /**
     * Sets the value.
     *
     * @param statement the statement
     * @param name the name of identifier to bind to
     * @param value the value
     */
    fun setValue(statement: Statement, name: String, value: T)

    /**
     * Returns the string presentation of the value.
     *
     * @param value the value
     * @return the string presentation of the value
     */
    fun toString(value: T): String
}
