package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * The store containing the retrieved entities.
 */
@ThreadSafe
interface EntityStore {

    /**
     * Whether the entity metamodel is contained in this store.
     *
     * @param metamodel the entity metamodel
     * @return whether the entity metamodel is contained in this store
     */
    operator fun contains(metamodel: EntityMetamodel<*, *, *>): Boolean

    /**
     * Returns an entity set.
     * @param metamodel the entity metamodel
     * @return the entity set
     */
    operator fun <T : Any> get(metamodel: EntityMetamodel<T, *, *>): Set<T>

    /**
     * Returns a one-to-one association.
     *
     * @param first the entity metamodel of the base side
     * @param second the entity metamodel of another side
     * @return the one-to-one association
     */
    fun <T : Any, S : Any> oneToOne(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<T, S?>

    /**
     * Returns a one-to-one association with the entity ID as the base-side type.
     *
     * @param first the entity metamodel of the base side
     * @param second the entity metamodel of another side
     * @return the one-to-one association
     */
    fun <T : Any, ID : Any, S : Any> oneToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<ID, S?>

    /**
     * Returns a one-to-many association.
     *
     * @param first the entity metamodel of the base side
     * @param second the entity metamodel of another side
     * @return the one-to-many association
     */
    fun <T : Any, S : Any> oneToMany(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<T, Set<S>>

    /**
     * Returns a one-to-many association with the entity ID as the base-side type.
     *
     * @param first the entity metamodel of the base side
     * @param second the entity metamodel of another side
     * @return the one-to-many association
     */
    fun <T : Any, ID : Any, S : Any> oneToManyById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<ID, Set<S>>

    fun <T : Any, S : Any> manyToOne(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<T, S?>

    fun <T : Any, ID : Any, S : Any> manyToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<ID, S?>

    /**
     * Finds a single entity of another side.
     *
     * @param first the entity metamodel of the base side
     * @param second the entity metamodel of another side
     * @param entity the entity of the base side
     * @return the single entity
     */
    fun <T : Any, ID : Any, S : Any> findOne(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
        entity: T,
    ): S?

    /**
     * Finds multiple entities of another side.
     *
     * @param first the entity metamodel of the base side
     * @param second the entity metamodel of another side
     * @param entity the entity of the base side
     * @return multiple entities
     */
    fun <T : Any, ID : Any, S : Any> findMany(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
        entity: T,
    ): Set<S>
}

internal class EntityStoreImpl(
    private val entitySets: Map<EntityMetamodel<*, *, *>, Set<Any>>,
    private val rows: List<Map<EntityMetamodel<*, *, *>, EntityRef<*, *, *>>>,
) : EntityStore {

    private val oneToManyCache: ConcurrentMap<Pair<*, *>, Map<EntityRef<*, *, *>, Set<*>>> = ConcurrentHashMap()
    private val manyToOneCache: ConcurrentMap<Pair<*, *>, Map<EntityRef<*, *, *>, *>> = ConcurrentHashMap()

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
        second: EntityMetamodel<S, *, *>,
    ): Map<T, S?> {
        return createOneToOne(first, second).mapKeys { it.key.entity }
    }

    override fun <T : Any, ID : Any, S : Any> oneToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<ID, S?> {
        return createOneToOne(first, second).mapKeys { it.key.id }
    }

    private fun <META : EntityMetamodel<T, ID, *>, T : Any, ID : Any, S : Any> createOneToOne(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<EntityRef<META, T, ID>, S?> {
        return createManyToOne(first, second)
    }

    override fun <T : Any, S : Any> oneToMany(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<T, Set<S>> {
        return createOneToMany(first, second).mapKeys { it.key.entity }
    }

    override fun <T : Any, ID : Any, S : Any> oneToManyById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<ID, Set<S>> {
        return createOneToMany(first, second).mapKeys { it.key.id }
    }

    private fun <META : EntityMetamodel<T, ID, *>, T : Any, ID : Any, S : Any> createOneToMany(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<EntityRef<META, T, ID>, Set<S>> {
        if (!contains(first, second)) {
            return emptyMap()
        }
        val cached = oneToManyCache.computeIfAbsent(first to second) {
            val oneToMany = mutableMapOf<EntityRef<*, *, *>, MutableSet<Any>>()
            for (row in rows) {
                val ref1 = row[first]
                val ref2 = row[second]
                if (ref1 != null) {
                    val values = oneToMany.computeIfAbsent(ref1) { mutableSetOf() }
                    if (ref2 != null) {
                        values.add(ref2.entity)
                    }
                }
            }
            oneToMany
        }
        @Suppress("UNCHECKED_CAST")
        return cached as Map<EntityRef<META, T, ID>, Set<S>>
    }

    override fun <T : Any, S : Any> manyToOne(
        first: EntityMetamodel<T, *, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<T, S?> {
        return createManyToOne(first, second).mapKeys { it.key.entity }
    }

    override fun <T : Any, ID : Any, S : Any> manyToOneById(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<ID, S?> {
        return createManyToOne(first, second).mapKeys { it.key.id }
    }

    private fun <META : EntityMetamodel<T, ID, *>, T : Any, ID : Any, S : Any> createManyToOne(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
    ): Map<EntityRef<META, T, ID>, S?> {
        if (!contains(first, second)) {
            return emptyMap()
        }
        val cached = manyToOneCache.computeIfAbsent(first to second) {
            val manyToOne = mutableMapOf<EntityRef<*, *, *>, Any?>()
            for (row in rows) {
                val ref1 = row[first]
                val ref2 = row[second]
                if (ref1 != null) {
                    manyToOne[ref1] = ref2?.entity
                }
            }
            manyToOne
        }
        @Suppress("UNCHECKED_CAST")
        return cached as Map<EntityRef<META, T, ID>, S?>
    }

    override fun <T : Any, ID : Any, S : Any> findOne(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
        entity: T,
    ): S? {
        val manyToOne = createManyToOne(first, second)
        val key = EntityRef(first, entity)
        return manyToOne[key]
    }

    override fun <T : Any, ID : Any, S : Any> findMany(
        first: EntityMetamodel<T, ID, *>,
        second: EntityMetamodel<S, *, *>,
        entity: T,
    ): Set<S> {
        val oneToMany = createOneToMany(first, second)
        val key = EntityRef(first, entity)
        return oneToMany[key] ?: emptySet()
    }
}

internal class EntityRef<META : EntityMetamodel<ENTITY, ID, *>, ENTITY : Any, ID : Any>(
    val metamodel: META,
    val entity: ENTITY,
) {
    val id: ID = metamodel.extractId(entity)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityRef<*, *, *>

        if (metamodel != other.metamodel) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metamodel.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
