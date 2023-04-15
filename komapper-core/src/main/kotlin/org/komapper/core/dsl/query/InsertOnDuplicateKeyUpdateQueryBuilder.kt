package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.scope.AssignmentScope

/**
 * The builder of the query that inserts or updates entities.
 *
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 * @param BUILDER the builder type
 */
@ThreadSafe
interface InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, BUILDER : InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META, BUILDER>> {
    /**
     * Sets the values to be updated.
     *
     * @param declaration the assignment declaration
     * @return the builder
     */
    fun set(declaration: AssignmentScope<ENTITY>.(META) -> Unit): BUILDER

    /**
     * Sets search conditions.
     *
     * @param declaration the where declaration
     * @return the builder
     * */
    fun where(declaration: WhereDeclaration): InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY, ID, META>

    /**
     * Builds a query to bulk insert or update a list of entities.
     *
     * @param entities the entities to be inserted or updated
     * @return the query
     */
    fun multiple(entities: List<ENTITY>): EntityUpsertMultipleQuery<ENTITY>

    /**
     * Builds a query to bulk insert or update an array of entities.
     *
     * @param entities the entities to be inserted or updated
     * @return the query
     */
    fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Long>

    /**
     * Builds a query to insert or update a list of entities in a batch.
     *
     * @param entities the entities to be inserted or updated
     * @return the query
     */
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityUpsertQuery<List<Long>>

    /**
     * Builds a query to insert or update an array of entities in a batch.
     *
     * @param entities the entities to be inserted or updated
     * @return the query
     */
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityUpsertQuery<List<Long>>

    /**
     * Builds a query to insert or update a single entity and get the result as a new entity.
     *
     * @param entity the entity to be inserted or updated
     * @return the query that returns a new entity
     */
    fun executeAndGet(entity: ENTITY): Query<ENTITY>
}

/**
 * The builder of the query that inserts or updates entities.
 * This builder provides the function to retrieve an inserted or updated entity as a non-nullable instance.
 *
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
interface InsertOnDuplicateKeyUpdateQueryBuilderReturningSingle<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META, InsertOnDuplicateKeyUpdateQueryBuilderReturningSingle<ENTITY, ID, META>> {
    /**
     * Builds a query to insert or update a single entity.
     * The query returns a non-nullable entity if [EntityUpsertSingleQuery.returning] is called.
     *
     * @param entity the entity to be inserted or updated
     * @return the query
     */
    fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY>
}

/**
 * The builder of the query that inserts or updates entities.
 * This builder provides the function to retrieve an inserted or updated entity as a nullable instance.
 *
 * @param ENTITY the entity type
 * @param ID the entity id type
 * @param META the entity metamodel type
 */
interface InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> : InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META, InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY, ID, META>> {
    /**
     * Builds a query to insert or update a single entity.
     * The query returns a nullable entity if [EntityUpsertSingleQuery.returning] is called.
     *
     * @param entity the entity to be inserted or updated
     * @return the query
     */
    fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY?>
}

internal data class InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : InsertOnDuplicateKeyUpdateQueryBuilderReturningSingle<ENTITY, ID, META> {

    private val builder: EntityUpsertQueryBuilderReturningSingle<ENTITY, ID, META> = EntityUpsertQueryBuilderReturningSingleImpl(context)

    override fun set(declaration: AssignmentDeclaration<ENTITY, META>): InsertOnDuplicateKeyUpdateQueryBuilderReturningSingle<ENTITY, ID, META> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY, ID, META> {
        val newContext = context.copy(where = context.where + declaration)
        return InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNullImpl(newContext)
    }

    override fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY> {
        return builder.single(entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertMultipleQuery<ENTITY> {
        return builder.multiple(entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityUpsertMultipleQuery<ENTITY> {
        return builder.multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return builder.batch(entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return builder.batch(entities.toList(), batchSize)
    }

    override fun executeAndGet(entity: ENTITY): Query<ENTITY> {
        return executeAndGet(context, entity)
    }
}

internal data class InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNullImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY, ID, META> {

    private val builder: EntityUpsertQueryBuilderReturningSingleOrNull<ENTITY, ID, META> = EntityUpsertQueryBuilderReturningSingleOrNullImpl(context)

    override fun set(declaration: AssignmentDeclaration<ENTITY, META>): InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY, ID, META> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): InsertOnDuplicateKeyUpdateQueryBuilderReturningSingleOrNull<ENTITY, ID, META> {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun single(entity: ENTITY): EntityUpsertSingleQuery<ENTITY?> {
        return builder.single(entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertMultipleQuery<ENTITY> {
        return builder.multiple(entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityUpsertMultipleQuery<ENTITY> {
        return builder.multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return builder.batch(entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return builder.batch(entities.toList(), batchSize)
    }

    override fun executeAndGet(entity: ENTITY): Query<ENTITY> {
        return executeAndGet(context, entity)
    }
}

private fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> executeAndGet(context: EntityUpsertContext<ENTITY, ID, META>, entity: ENTITY): Query<ENTITY> {
    return EntityUpsertSingleUpdateQuery(context, entity).flatMap { newEntity ->
        val keys = context.keys.map { it to it.getter(newEntity) }
        QueryDsl.from(context.target).where {
            for ((property, value) in keys) {
                @Suppress("UNCHECKED_CAST")
                property as ColumnExpression<Any, Any>
                if (value == null) {
                    property.isNull()
                } else {
                    property eq value
                }
            }
        }.first()
    }
}
