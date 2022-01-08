package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to insert entities.
 * This query returns new entity or entities.
 *
 * @param T the entity type
 */
interface EntityInsertQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<T>
}

internal data class EntityInsertSingleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY
) : EntityInsertQuery<ENTITY> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleQuery(context, entity)
    }
}

internal data class EntityInsertMultipleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) : EntityInsertQuery<List<ENTITY>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleQuery(context, entities)
    }
}

internal data class EntityInsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityInsertQuery<List<ENTITY>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertBatchQuery(context, entities)
    }
}
