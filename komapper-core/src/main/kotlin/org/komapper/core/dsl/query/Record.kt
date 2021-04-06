package org.komapper.core.dsl.query

import org.komapper.core.dsl.expr.PropertyExpression
import kotlin.reflect.cast

interface Record {
    operator fun <T : Any> get(key: PropertyExpression<T>): T?
}

internal class RecordImpl(private val map: Map<PropertyExpression<*>, Any?>) : Record {

    override fun <T : Any> get(key: PropertyExpression<T>): T? {
        val value = map[key]
        return if (value == null) null else key.klass.cast(value)
    }

    operator fun <T : Any> contains(key: PropertyExpression<T>) = map.contains(key)

    operator fun iterator() = map.iterator()
}
