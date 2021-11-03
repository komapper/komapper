package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression
import kotlin.reflect.cast

@ThreadSafe
interface Record<K> {
    val keys: Set<K>
    val values: Collection<Any?>
    val entries: Set<Map.Entry<K, Any?>>
    operator fun contains(key: K): Boolean
    operator fun iterator(): Iterator<Map.Entry<K, Any?>>
}

interface Columns : Record<ColumnExpression<*, *>> {
    operator fun <T : Any> get(key: ColumnExpression<T, *>): T?
}

abstract class AbstractRecord<K>(protected val map: Map<K, Any?>) : Record<K> {
    override val keys: Set<K>
        get() = map.keys

    override val values: Collection<Any?>
        get() = map.values

    override val entries: Set<Map.Entry<K, Any?>>
        get() = map.entries

    override fun contains(key: K) = map.contains(key)

    override fun iterator() = map.iterator()
}

class ColumnsImpl(
    map: Map<ColumnExpression<*, *>, Any?>
) : AbstractRecord<ColumnExpression<*, *>>(map), Columns {

    override fun <T : Any> get(key: ColumnExpression<T, *>): T? {
        val value = map[key]
        return if (value == null) null else key.exteriorClass.cast(value)
    }
}
