package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.cast

@ThreadSafe
interface EntityContext<ENTITY> {
    val mainEntities: List<ENTITY>
    operator fun contains(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Boolean
    fun <T : Any, S : Any> associate(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): OneToMany<T, S>
    fun <T : Any, S : Any, ID> associateById(pair: Pair<EntityMetamodel<T, ID, *>, EntityMetamodel<S, *, *>>): OneToMany<ID, S>
}

@ThreadSafe
interface OneToOne<T, S> : Map<T, S?>

@ThreadSafe
interface OneToMany<T, S> : Map<T, Set<S>> {
    fun asOneToOne(): OneToOne<T, S> {
        val map = mapValues { it.value.firstOrNull() }
        return OneToOneImpl(map)
    }
}

internal class EntityContextImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val rows: List<Map<EntityKey, Any>>,
) : EntityContext<ENTITY> {

    private val cache: ConcurrentMap<Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>, Map<Any, Set<Any>>> =
        ConcurrentHashMap()

    override val mainEntities: List<ENTITY> by lazy {
        val metamodel = context.target
        rows.asSequence()
            .flatMap { it.values }
            .filter(metamodel.klass()::isInstance)
            .map(metamodel.klass()::cast)
            .distinctBy { metamodel.getId(it) }
            .toList()
    }

    override fun contains(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Boolean {
        val metamodels = context.projection.metamodels
        return pair.first in metamodels && pair.second in metamodels
    }

    override fun <T : Any, S : Any> associate(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): OneToMany<T, S> {
        return if (contains(pair)) {
            val oneToMany = cache.computeIfAbsent(pair) { createOneToMany(it) }
            @Suppress("UNCHECKED_CAST")
            oneToMany as OneToMany<T, S>
        } else {
            OneToManyImpl(emptyMap())
        }
    }

    override fun <T : Any, S : Any, ID> associateById(pair: Pair<EntityMetamodel<T, ID, *>, EntityMetamodel<S, *, *>>): OneToMany<ID, S> {
        val oneToMany = associate(pair)
        val metamodel = pair.first
        return OneToManyImpl(oneToMany.mapKeys { metamodel.getId(it.key) })
    }

    private fun createOneToMany(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): OneToMany<Any, Any> {
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
        return OneToManyImpl(oneToMany)
    }
}

internal class OneToOneImpl<T, S>(private val map: Map<T, S?>) : Map<T, S?> by map, OneToOne<T, S>

internal class OneToManyImpl<T, S>(private val map: Map<T, Set<S>>) : Map<T, Set<S>> by map, OneToMany<T, S>
