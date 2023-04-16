package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to update entities.
 * This query returns new entity or entities.
 *
 * @param T the entity type
 */
interface EntityUpdateQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<T>
}

interface EntityUpdateSingleQuery<ENTITY> :
    EntityUpdateQuery<ENTITY> {
    fun returning(): EntityUpdateReturningQuery<ENTITY>
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateSingleQuery<ENTITY>
}

internal data class EntityUpdateSingleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpdateSingleQuery<T> {
    override fun returning(): EntityUpdateReturningQuery<T> {
        val newContext = context.copy(returning = true)
        return EntityUpdateSingleReturningQuery(newContext, entity)
    }

    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateSingleQuery<T> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleQuery(context, entity)
    }
}

internal data class EntityUpdateBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpdateQuery<List<ENTITY>> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateBatchQuery(context, entities)
    }
}
