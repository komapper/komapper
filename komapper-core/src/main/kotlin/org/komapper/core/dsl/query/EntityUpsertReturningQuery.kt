package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to upsert entities and retrieve inserted or updated entities.
 *
 * @param T the result type
 */
interface EntityUpsertReturningQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<T>
}

internal data class EntityUpsertSingleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertReturningQuery<T> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<T> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleReturningQuery(context, entity)
    }
}

internal data class EntityUpsertMultipleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertReturningQuery<List<ENTITY>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<List<ENTITY>> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleReturningQuery(context, entities)
    }
}
