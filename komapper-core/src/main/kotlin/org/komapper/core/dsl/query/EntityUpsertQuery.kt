package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to upsert entities.
 * This query returns a numeric value or a list of numeric values specific to the driver.
 *
 * @param T the result type
 */
interface EntityUpsertQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<T>
}

/**
 * Represents a query to upsert a single entity.
 *
 * @param ENTITY the entity type
 */
interface EntityUpsertSingleQuery<ENTITY> : EntityUpsertQuery<Long> {
    /**
     * Indicates to retrieve an upserted entity.
     */
    fun returning(): EntityUpsertReturningQuery<ENTITY>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertSingleQuery<ENTITY>
}

/**
 * Represents a query to upsert multiple entities.
 *
 * @param ENTITY the entity type
 */
interface EntityUpsertMultipleQuery<ENTITY : Any> : EntityUpsertQuery<Long> {
    /**
     * Indicates to retrieve upserted entities.
     */
    fun returning(): EntityUpsertReturningQuery<List<ENTITY>>

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertMultipleQuery<ENTITY>
}

internal data class EntityUpsertSingleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertSingleQuery<T> {
    override fun returning(): EntityUpsertReturningQuery<T> {
        val newContext = context.copy(returning = true)
        return EntityUpsertSingleReturningQuery(newContext, entity)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertSingleQuery<T> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleQuery(context, entity)
    }
}

internal data class EntityUpsertSingleUpdateQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : Query<ENTITY> {

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleUpdateQuery(context, entity)
    }
}

internal data class EntityUpsertSingleIgnoreQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertQuery<ENTITY?> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<ENTITY?> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleIgnoreQuery(context, entity)
    }
}

internal data class EntityUpsertMultipleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertMultipleQuery<ENTITY> {
    override fun returning(): EntityUpsertReturningQuery<List<ENTITY>> {
        val newContext = context.copy(returning = true)
        return EntityUpsertMultipleReturningQuery(newContext, entities)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertMultipleQuery<ENTITY> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleQuery(context, entities)
    }
}

data class EntityUpsertBatchQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertQuery<List<Long>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<List<Long>> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertBatchQuery(context, entities)
    }
}
