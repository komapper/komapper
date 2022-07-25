package org.komapper.jdbc.spi

import org.komapper.core.ThreadSafe
import java.sql.JDBCType
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

@ThreadSafe
interface JdbcUserDataType<T : Any> {
    /**
     * The data type name.
     */
    val name: String

    /**
     * The corresponding class.
     */
    val klass: KClass<T>

    /**
     * The JDBC type defined in the standard library.
     */
    val jdbcType: JDBCType

    /**
     * Returns the value.
     *
     * @param rs the result set
     * @param index the index
     * @return the value
     */
    fun getValue(rs: ResultSet, index: Int): T?

    /**
     * Returns the value.
     * @param rs the result set
     * @param columnLabel the column label
     * @return the value
     */
    fun getValue(rs: ResultSet, columnLabel: String): T?

    /**
     * Sets the value.
     *
     * @param ps the prepared statement
     * @param index the index
     */
    fun setValue(ps: PreparedStatement, index: Int, value: T)

    /**
     * Returns the string presentation of the value.
     *
     * @param value the value
     * @return the string presentation of the value
     */
    fun toString(value: T): String
}
