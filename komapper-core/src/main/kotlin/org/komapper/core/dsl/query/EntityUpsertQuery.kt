package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to upsert entities.
 * This query returns a numeric value or a list of numeric values specific to the driver.
 *
 * @param T the entity type
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

internal data class EntityUpsertSingleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpsertQuery<Long> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Long> {
        return copy(context = context.copy(configure))
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
        return copy(context = context.copy(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleIgnoreQuery(context, entity)
    }
}

internal data class EntityUpsertMultipleQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityUpsertQuery<Long> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertQuery<Long> {
        return copy(context = context.copy(configure))
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
        return copy(context = context.copy(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertBatchQuery(context, entities)
    }
}

private fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityUpsertContext<ENTITY, ID, META>.copy(
    configure: (InsertOptions) -> InsertOptions,
): EntityUpsertContext<ENTITY, ID, META> {
    val options = configure(this.insertContext.options)
    val insertContext = this.insertContext.copy(options = options)
    return this.copy(insertContext = insertContext)
}
