package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression
import kotlin.reflect.cast

/**
 * Represents a single row detached from the result set obtained by query execution.
 */
@ThreadSafe
interface Record {
    /**
     * The columns contained in this record.
     */
    val keys: Set<ColumnExpression<*, *>>

    /**
     * The values contained in this record.
     */
    val values: Collection<Any?>

    /**
     * The columns and values contained in this record.
     */
    val entries: Set<Map.Entry<ColumnExpression<*, *>, Any?>>

    /**
     * Returns whether the column is contained.
     *
     * @param key the column
     * @return true if the column is contained
     */
    operator fun contains(key: ColumnExpression<*, *>): Boolean

    /**
     * @return the iterator
     */
    operator fun iterator(): Iterator<Map.Entry<ColumnExpression<*, *>, Any?>>

    /**
     * Returns the value of the specified column.
     *
     * @param T the type of the value
     * @param key the column
     */
    operator fun <T : Any> get(key: ColumnExpression<T, *>): T?
}

class RecordImpl(private val map: Map<ColumnExpression<*, *>, Any?>) : Record {
    override val keys: Set<ColumnExpression<*, *>>
        get() = map.keys

    override val values: Collection<Any?>
        get() = map.values

    override val entries: Set<Map.Entry<ColumnExpression<*, *>, Any?>>
        get() = map.entries

    override fun contains(key: ColumnExpression<*, *>) = map.contains(key)

    override fun iterator() = map.iterator()

    override fun <T : Any> get(key: ColumnExpression<T, *>): T? {
        val value = map[key]
        return if (value == null) null else key.exteriorClass.cast(value)
    }
}
