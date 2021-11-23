package org.komapper.core.dsl.runner

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityContext
import org.komapper.core.dsl.query.OneToMany
import org.komapper.core.dsl.query.OneToOne
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.cast

internal class EntityContextImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val pool: Map<EntityKey, Any>,
    private val rows: List<Map<EntityMetamodel<*, *, *>, Any>>,
) : EntityContext<ENTITY> {

    private val oneToManyCache: ConcurrentMap<Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>, Map<Any, Set<Any>>> =
        ConcurrentHashMap()

    override val mainEntities: List<ENTITY> by lazy {
        val metamodel = context.target
        val klass = metamodel.klass()
        pool.asSequence()
            .filter { it.key.entityMetamodel == metamodel }
            .map { klass.cast(it.value) }
            .distinctBy { metamodel.id(it) }
            .toList()
    }

    override fun contains(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): Boolean {
        val metamodels = context.projection.metamodels()
        return pair.first in metamodels && pair.second in metamodels
    }

    override fun <T : Any, S : Any> associate(pair: Pair<EntityMetamodel<T, *, *>, EntityMetamodel<S, *, *>>): OneToMany<T, S> {
        return if (contains(pair)) {
            val oneToMany = oneToManyCache.computeIfAbsent(pair) { createOneToMany(it) }
            @Suppress("UNCHECKED_CAST")
            oneToMany as OneToMany<T, S>
        } else {
            OneToManyImpl(emptyMap())
        }
    }

    override fun <T : Any, S : Any, ID : Any> associateById(pair: Pair<EntityMetamodel<T, ID, *>, EntityMetamodel<S, *, *>>): OneToMany<ID, S> {
        val oneToMany = associate(pair)
        val metamodel = pair.first
        return OneToManyImpl(oneToMany.mapKeys { metamodel.id(it.key) })
    }

    private fun createOneToMany(pair: Pair<EntityMetamodel<*, *, *>, EntityMetamodel<*, *, *>>): OneToMany<Any, Any> {
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
        return OneToManyImpl(oneToMany)
    }
}

internal class OneToOneImpl<T, S>(private val map: Map<T, S?>) : Map<T, S?> by map, OneToOne<T, S>

internal class OneToManyImpl<T, S>(private val map: Map<T, Set<S>>) : Map<T, Set<S>> by map, OneToMany<T, S> {
    override fun asOneToOne(): OneToOne<T, S> {
        val map = mapValues { it.value.firstOrNull() }
        return OneToOneImpl(map)
    }
}
