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
 */
@ThreadSafe
interface InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> {
    /**
     * Sets the values to be updated.
     *
     * @param declaration the assignment declaration
     * @return the builder
     */
    fun set(declaration: AssignmentScope<ENTITY>.(META) -> Unit): InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META>

    /**
     * Sets search conditions.
     *
     * @param declaration the where declaration
     * @return the builder
     * */
    fun where(declaration: WhereDeclaration): InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META>

    /**
     * Builds a query to insert or update a single entity.
     *
     * @param entity the entity to be inserted or updated
     * @return the query
     */
    fun single(entity: ENTITY): EntityUpsertQuery<Long>

    /**
     * Builds a query to bulk insert or update a list of entities.
     *
     * @param entities the entities to be inserted or updated
     * @return the query
     */
    fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Long>

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

internal data class InsertOnDuplicateKeyUpdateQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
) : InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META> {

    private val builder: EntityUpsertQueryBuilder<ENTITY, ID, META> = EntityUpsertQueryBuilderImpl(context)

    override fun set(declaration: AssignmentDeclaration<ENTITY, META>): InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(set = context.set + declaration)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): InsertOnDuplicateKeyUpdateQueryBuilder<ENTITY, ID, META> {
        val newContext = context.copy(where = context.where + declaration)
        return copy(context = newContext)
    }

    override fun single(entity: ENTITY): EntityUpsertQuery<Long> {
        return builder.single(entity)
    }

    override fun multiple(entities: List<ENTITY>): EntityUpsertQuery<Long> {
        return builder.multiple(entities)
    }

    override fun multiple(vararg entities: ENTITY): EntityUpsertQuery<Long> {
        return builder.multiple(entities.toList())
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return builder.batch(entities, batchSize)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityUpsertQuery<List<Long>> {
        return builder.batch(entities.toList(), batchSize)
    }

    override fun executeAndGet(entity: ENTITY): Query<ENTITY> {
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
}
