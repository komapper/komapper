package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Association
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface EntityAggregate {
    fun <T : Any, S : Any> oneToOne(
        keyModel: EntityMetamodel<T, *, *>,
        valueModel: EntityMetamodel<S, *, *>
    ): Map<T, S?>

    fun <T : Any, S : Any> oneToMany(
        keyModel: EntityMetamodel<T, *, *>,
        valueModel: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>>
}

internal class EntityAggregateImpl(private val associations: Map<Association, Map<Any, Set<Any>>>) : EntityAggregate {
    override fun <T : Any, S : Any> oneToOne(
        keyModel: EntityMetamodel<T, *, *>,
        valueModel: EntityMetamodel<S, *, *>
    ): Map<T, S?> {
        val oneToMany = oneToMany(keyModel, valueModel)
        return oneToMany.mapValues { it.value.firstOrNull() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any, S : Any> oneToMany(
        keyModel: EntityMetamodel<T, *, *>,
        valueModel: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>> {
        val result = associations[keyModel to valueModel] ?: emptyMap()
        return result as Map<T, Set<S>>
    }
}
