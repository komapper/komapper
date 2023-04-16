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
interface EntityUpdateReturningQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateReturningQuery<T>
}

internal data class EntityUpdateSingleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpdateReturningQuery<T> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateReturningQuery<T> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleReturningQuery(context, entity)
    }
}
