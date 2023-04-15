package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to insert entities and retrieve inserted entities.
 * This query returns new entity or entities.
 *
 * @param T the result type
 */
interface EntityInsertReturningQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<T>
}

internal data class EntityInsertSingleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityInsertReturningQuery<ENTITY> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleReturningQuery(context, entity)
    }
}

internal data class EntityInsertMultipleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityInsertReturningQuery<List<ENTITY>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleReturningQuery(context, entities)
    }
}
