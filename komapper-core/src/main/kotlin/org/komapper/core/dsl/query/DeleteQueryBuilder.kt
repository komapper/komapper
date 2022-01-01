package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions

/**
 * The builder of delete queries.
 * @param ENTITY the entity type
 */
@ThreadSafe
interface DeleteQueryBuilder<ENTITY : Any> {
    /**
     * Builds a query to delete a single entity.
     * @param entity the entity to be deleted
     * @return the query
     */
    fun single(entity: ENTITY): EntityDeleteQuery

    /**
     * Builds a query to delete a list of entities in a batch.
     * @param entities the entities to be deleted
     * @param batchSize the batch size. If it is null, the value of [DeleteOptions.batchSize] will be used.
     * @return the query
     */
    fun batch(entities: List<ENTITY>, batchSize: Int? = null): EntityDeleteQuery

    /**
     * Builds a query to delete an array of entities in a batch.
     * @param entities the entities to be deleted
     * @param batchSize the batch size. If it is null, the value of [DeleteOptions.batchSize] will be used.
     * @return the query
     */
    fun batch(vararg entities: ENTITY, batchSize: Int? = null): EntityDeleteQuery

    /**
     * Builds a WHERE clause.
     * @param declaration the where declaration
     * @return the query
     */
    fun where(declaration: WhereDeclaration): RelationDeleteQuery

    /**
     * Builds a query to delete all rows.
     * @return the query
     */
    fun all(): RelationDeleteQuery
}

internal data class DeleteQueryBuilderImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
) :
    DeleteQueryBuilder<ENTITY> {

    override fun single(entity: ENTITY): EntityDeleteQuery {
        context.target.checkIdValueNotNull(entity)
        return EntityDeleteSingleQuery(context, entity)
    }

    override fun batch(entities: List<ENTITY>, batchSize: Int?): EntityDeleteQuery {
        context.target.checkIdValueNotNull(entities)
        val context = if (batchSize != null) {
            context.copy(options = context.options.copy(batchSize = batchSize))
        } else context
        return EntityDeleteBatchQuery(context, entities)
    }

    override fun batch(vararg entities: ENTITY, batchSize: Int?): EntityDeleteQuery {
        return batch(entities.toList(), batchSize)
    }

    override fun where(declaration: WhereDeclaration): RelationDeleteQuery {
        return asRelationDeleteQuery().where(declaration)
    }

    override fun all(): RelationDeleteQuery {
        return asRelationDeleteQuery()
    }

    private fun asRelationDeleteQuery(): RelationDeleteQuery {
        return RelationDeleteQueryImpl(context.asRelationDeleteContext())
    }
}
