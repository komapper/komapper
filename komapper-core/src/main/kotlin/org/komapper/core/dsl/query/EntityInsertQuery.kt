package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to insert entities.
 * This query returns new entity or entities.
 *
 * @param T the result type
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

/**
 * Represents a query to insert a single entity.
 *
 * @param ENTITY the entity type
 */
interface EntityInsertSingleQuery<ENTITY : Any> : EntityInsertQuery<ENTITY> {
    /**
     * Indicates to retrieve an inserted entity.
     */
    fun returning(): EntityInsertReturningQuery<ENTITY>
}

/**
 * Represents a query to insert multiple entities.
 *
 * @param ENTITY the entity type
 */
interface EntityInsertMultipleQuery<ENTITY : Any> : EntityInsertQuery<List<ENTITY>> {
    /**
     * Indicates to retrieve inserted entities.
     */
    fun returning(): EntityInsertReturningQuery<List<ENTITY>>
}

internal data class EntityInsertSingleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityInsertSingleQuery<ENTITY> {
    override fun returning(): EntityInsertReturningQuery<ENTITY> {
        val newContext = context.copy(returning = true)
        return EntityInsertSingleReturningQuery(newContext, entity)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertQuery<ENTITY> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleQuery(context, entity)
    }
}

internal data class EntityInsertMultipleQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : EntityInsertMultipleQuery<ENTITY> {
    override fun returning(): EntityInsertReturningQuery<List<ENTITY>> {
        val newContext = context.copy(returning = true)
        return EntityInsertMultipleReturningQuery(newContext, entities)
    }

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertMultipleQuery<ENTITY> {
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
