package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents the query to delete entities.
 * This query returns Unit.
 */
interface EntityDeleteQuery : Query<Unit> {
    /**
     * Builds a query with the options applied.
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery
}

internal data class EntityDeleteSingleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY
) : EntityDeleteQuery {

    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleQuery(context, entity)
    }
}

internal data class EntityDeleteBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityDeleteQuery {

    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteQuery {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteBatchQuery(context, entities)
    }
}
