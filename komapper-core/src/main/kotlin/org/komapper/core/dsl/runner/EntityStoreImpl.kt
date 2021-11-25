package org.komapper.core.dsl.runner

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class EntityStoreImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val rows: List<Map<EntityMetamodel<*, *, *>, Any>>,
) : EntityStore<ENTITY> {

    private val oneToManyCache: ConcurrentMap<Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>, Map<Any, Set<Any>>> =
        ConcurrentHashMap()

    override fun contains(metamodel: EntityMetamodel<*, *, *>): Boolean {
        return metamodel in context.getProjection().metamodels()
    }

    override fun contains(first: EntityMetamodel<*, *, *>, second: EntityMetamodel<*, *, *>): Boolean {
        val metamodels = context.getProjection().metamodels()
        return first in metamodels && second in metamodels
    }

    override fun <T : Any> list(metamodel: EntityMetamodel<T, *, *>): List<T> {
        val oneToMany = oneToMany(metamodel, metamodel)
        return oneToMany.keys.toList()
    }

    override fun <T : Any, S : Any> oneToOne(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, S?> {
        val oneToMany = oneToMany(first, second)
        return oneToMany.mapValues { it.value.firstOrNull() }
    }

    override fun <T : Any, ID : Any, S : Any> oneToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<ID, S?> {
        val oneToMany = oneToManyById(first, second)
        return oneToMany.mapValues { it.value.firstOrNull() }
    }

    override fun <T : Any, S : Any> oneToMany(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>> {
        val pair = first to second
        return if (contains(pair.first, pair.second)) {
            val oneToMany = oneToManyCache.computeIfAbsent(pair) { createOneToMany(it) }
            @Suppress("UNCHECKED_CAST")
            oneToMany as Map<T, Set<S>>
        } else {
            emptyMap()
        }
    }

    override fun <T : Any, ID : Any, S : Any> oneToManyById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<ID, Set<S>> {
        val oneToMany = oneToMany(first, second)
        return oneToMany.mapKeys { first.id(it.key) }
    }

    private fun createOneToMany(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Map<Any, Set<Any>> {
        val oneToMany = mutableMapOf<Any, MutableSet<Any>>()
        for (row in rows) {
            val entity1 = row[pair.first]
            val entity2 = row[pair.second]
            if (entity1 != null) {
                val values = oneToMany.computeIfAbsent(entity1) { mutableSetOf() }
                if (entity2 != null) {
                    values.add(entity2)
                }
            }
        }
        return oneToMany
    }
}
