package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.cast

@ThreadSafe
interface EntityAggregate<ENTITY> {
    val entities: List<ENTITY>
    fun hasAssociation(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Boolean
    fun <T : Any, S : Any> oneToOne(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): Map<T, S?>
    fun <T : Any, S : Any> oneToMany(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): Map<T, Set<S>>
}

internal class EntityAggregateImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val rows: List<Map<EntityKey, Any>>,
) : EntityAggregate<ENTITY> {

    private val cache: ConcurrentMap<Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>, Map<Any, Set<Any>>> =
        ConcurrentHashMap()

    override val entities: List<ENTITY> by lazy {
        val metamodel = context.target
        rows.asSequence()
            .flatMap { it.values }
            .filter(metamodel.klass()::isInstance)
            .map(metamodel.klass()::cast)
            .distinctBy { metamodel.getId(it) }
            .toList()
    }

    override fun hasAssociation(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Boolean {
        val metamodels = context.projection.metamodels
        return pair.first in metamodels && pair.second in metamodels
    }

    override fun <T : Any, S : Any> oneToOne(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): Map<T, S?> {
        val oneToMany = oneToMany(pair)
        return oneToMany.mapValues { it.value.firstOrNull() }
    }

    override fun <T : Any, S : Any> oneToMany(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): Map<T, Set<S>> {
        return if (hasAssociation(pair)) {
            val oneToMany = cache.computeIfAbsent(pair) { createOneToMany(it) }
            @Suppress("UNCHECKED_CAST")
            oneToMany as Map<T, Set<S>>
        } else {
            emptyMap()
        }
    }

    private fun createOneToMany(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Map<Any, Set<Any>> {
        val oneToMany = mutableMapOf<Any, MutableSet<Any>>()
        // hold only unique entities
        val pool: MutableMap<EntityKey, Any> = mutableMapOf()
        for (row in rows) {
            val keys: MutableMap<EntityMetamodel<*, *, *>, EntityKey> = mutableMapOf()
            for ((key, entity) in row) {
                pool.putIfAbsent(key, entity)
                keys[key.entityMetamodel] = key
            }
            val key1 = keys[pair.first]
            val key2 = keys[pair.second]
            if (key1 != null) {
                val entity1 = pool[key1]!!
                val entity2 = pool[key2]
                val values = oneToMany.computeIfAbsent(entity1) { mutableSetOf() }
                if (entity2 != null) {
                    values.add(entity2)
                }
            }
        }
        return oneToMany
    }
}
