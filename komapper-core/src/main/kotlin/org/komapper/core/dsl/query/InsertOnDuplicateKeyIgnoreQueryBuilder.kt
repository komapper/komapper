package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

/**
 * Represents the builder of the query that inserts entities and ignores duplicate keys.
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
@ThreadSafe
interface InsertOnDuplicateKeyIgnoreQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    /**
     * Builds a query to insert a single entity.
     * @param entity the entity to be inserted
     * @return the query
     */
    fun single(entity: ENTITY): EntityUpsertQuery<Int>

    /**
     * Builds a query to bulk insert a list of entities.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int>

    /**
     * Builds a query to bulk insert an array of entities.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Int>

    /**
     * Builds a query to bulk insert a list of entities.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpsertQuery<List<Int>>

    /**
     * Builds a query to bulk insert an array of entities.
     * @param entities the entities to be inserted
     * @return the query
     */
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityUpsertQuery<List<Int>>

    /**
     * Builds a query to insert or update a single entity and get the result as a new entity.
     * @param entity the entity to be inserted
     * @return the query that returns a new entity when the entity is inserted, or null when the entity has duplicate keys
     */
    fun executeAndGet(entity: ENTITY): Query<ENTITY?>
}

internal data class InsertOnDuplicateKeyIgnoreQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : InsertOnDuplicateKeyIgnoreQueryBuilder<ENTITY, ID, META> {

    private val builder: EntityUpsertQueryBuilder<ENTITY, ID, META> = EntityUpsertQueryBuilderImpl(context)

    override fun single(entity: ENTITY): EntityUpsertQuery<Int> {
        return builder.single(entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Int> {
        return builder.multiple(entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Int> {
        return builder.multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        return builder.batch(entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpsertQuery<List<Int>> {
        return builder.batch(entities.toList(), batchSize)
    }

    override fun executeAndGet(entity: ENTITY): EntityUpsertQuery<ENTITY?> {
        return EntityUpsertSingleIgnoreQuery(context, entity)
    }
}
