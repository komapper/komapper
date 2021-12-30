package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.reflect.cast

@ThreadSafe
interface EntityStore {

    operator fun contains(metamodel: EntityMetamodel<*, *, *>): Boolean

    operator fun <T : Any> get(metamodel: EntityMetamodel<T, *, *>): Set<T>

    fun <T : Any, S : Any> oneToOne(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, S?>

    fun <T : Any, ID : Any, S : Any> oneToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<ID, S?>

    fun <T : Any, S : Any> oneToMany(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>>

    fun <T : Any, ID : Any, S : Any> oneToManyById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<ID, Set<S>>
}

internal class EntityStoreImpl(
    private val entitySets: Map<EntityMetamodel<*, *, *>, Set<Any>>,
    private val rows: List<Map<EntityMetamodel<*, *, *>, Any>>
) : EntityStore {

    override operator fun contains(metamodel: EntityMetamodel<*, *, *>): Boolean {
        return entitySets.containsKey(metamodel)
    }

    private fun contains(first: EntityMetamodel<*, *, *>, second: EntityMetamodel<*, *, *>): Boolean {
        return entitySets.contains(first) && entitySets.containsKey(second)
    }

    override operator fun <T : Any> get(metamodel: EntityMetamodel<T, *, *>): Set<T> {
        @Suppress("UNCHECKED_CAST")
        return entitySets[metamodel] as Set<T>? ?: emptySet()
    }

    override fun <T : Any, S : Any> oneToOne(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, S?> {
        val oneToMany = createOneToMany(first, second)
        return oneToMany.mapValues { it.value.firstOrNull() }
    }

    override fun <T : Any, ID : Any, S : Any> oneToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<ID, S?> {
        val oneToMany = createOneToMany(first, second)
        return oneToMany.mapKeys { first.id(it.key) }.mapValues { it.value.firstOrNull() }
    }

    override fun <T : Any, S : Any> oneToMany(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>> {
        return createOneToMany(first, second)
    }

    override fun <T : Any, ID : Any, S : Any> oneToManyById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<ID, Set<S>> {
        val oneToMany = createOneToMany(first, second)
        return oneToMany.mapKeys { first.id(it.key) }
    }

    private fun <T : Any, S : Any> createOneToMany(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>
    ): Map<T, Set<S>> {
        if (!contains(first, second)) {
            return emptyMap()
        }
        val oneToMany = mutableMapOf<T, MutableSet<S>>()
        for (row in rows) {
            val entity1 = row[first]
            val entity2 = row[second]
            if (entity1 != null) {
                val key = first.klass().cast(entity1)
                val values = oneToMany.computeIfAbsent(key) { mutableSetOf() }
                if (entity2 != null) {
                    val value = second.klass().cast(entity2)
                    values.add(value)
                }
            }
        }
        return oneToMany
    }
}
