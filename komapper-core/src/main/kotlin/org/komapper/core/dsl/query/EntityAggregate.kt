package org.komapper.core.dsl.query

import org.komapper.core.dsl.element.Association
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface EntityAggregate {
    fun <T : Any, S : Any> oneToOne(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>
    ): Map<T, S?>

    fun <T : Any, S : Any> oneToMany(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>>
}

internal class EntityAggregateImpl(private val associations: Map<Association, Map<Any, Set<Any>>>) : EntityAggregate {
    override fun <T : Any, S : Any> oneToOne(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>
    ): Map<T, S?> {
        val oneToMany = oneToMany(metamodel1, metamodel2)
        return oneToMany.mapValues { it.value.firstOrNull() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any, S : Any> oneToMany(
        metamodel1: EntityMetamodel<T, *, *>,
        metamodel2: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>> {
        val result = associations[metamodel1 to metamodel2] ?: emptyMap()
        return result as Map<T, Set<S>>
    }
}
