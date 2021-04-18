package org.komapper.core.dsl.query

import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.reflect.cast

interface Record<K> {
    val keys: Set<K>
    val values: Collection<Any?>
    val entries: Set<Map.Entry<K, Any?>>
    operator fun contains(key: K): Boolean
    operator fun iterator(): Iterator<Map.Entry<K, Any?>>
}

interface PropertyRecord : Record<PropertyExpression<*>> {
    operator fun <T : Any> get(key: PropertyExpression<T>): T?
}

interface EntityRecord : Record<EntityMetamodel<*, *>> {
    operator fun <T : Any> get(key: EntityMetamodel<T, *>): T?
}

internal abstract class AbstractRecord<K>(protected val map: Map<K, Any?>) : Record<K> {
    override val keys: Set<K>
        get() = map.keys

    override val values: Collection<Any?>
        get() = map.values

    override val entries: Set<Map.Entry<K, Any?>>
        get() = map.entries

    override fun contains(key: K) = map.contains(key)

    override fun iterator() = map.iterator()
}

internal class PropertyRecordImpl(
    map: Map<PropertyExpression<*>, Any?>
) : AbstractRecord<PropertyExpression<*>>(map), PropertyRecord {

    override fun <T : Any> get(key: PropertyExpression<T>): T? {
        val value = map[key]
        return if (value == null) null else key.klass.cast(value)
    }
}

internal class EntityRecordImpl(
    map: Map<EntityMetamodel<*, *>, Any?>
) : AbstractRecord<EntityMetamodel<*, *>>(map), EntityRecord {

    override fun <T : Any> get(key: EntityMetamodel<T, *>): T? {
        val value = map[key]
        @Suppress("UNCHECKED_CAST")
        return if (value == null) null else value as T?
    }
}
