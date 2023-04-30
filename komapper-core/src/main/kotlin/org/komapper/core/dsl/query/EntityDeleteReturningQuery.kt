package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to delete an entity and retrieve the deleted values.
 *
 * @param T the result type
 */
interface EntityDeleteReturningQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteReturningQuery<T>
}

internal data class EntityDeleteSingleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityDeleteReturningQuery<ENTITY?> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteReturningQuery<ENTITY?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleReturningQuery(context, entity)
    }
}

internal data class EntityDeleteSingleReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expression: ColumnExpression<A, *>,
) : EntityDeleteReturningQuery<A?> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteReturningQuery<A?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleReturningSingleColumnQuery(context, entity, expression)
    }
}

internal data class EntityDeleteSingleReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : EntityDeleteReturningQuery<Pair<A?, B?>?> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteReturningQuery<Pair<A?, B?>?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleReturningPairColumnsQuery(context, entity, expressions)
    }
}

internal data class EntityDeleteSingleReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : EntityDeleteReturningQuery<Triple<A?, B?, C?>?> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): EntityDeleteReturningQuery<Triple<A?, B?, C?>?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityDeleteSingleReturningTripleColumnsQuery(context, entity, expressions)
    }
}
