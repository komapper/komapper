package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.expression.ColumnExpression
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

internal data class EntityUpdateSingleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : EntityUpdateReturningQuery<ENTITY?> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateReturningQuery<ENTITY?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleReturningQuery(context, entity)
    }
}

internal data class EntityUpdateSingleReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expression: ColumnExpression<A, *>,
) : EntityUpdateReturningQuery<A?> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateReturningQuery<A?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleReturningSingleColumnQuery(context, entity, expression)
    }
}

internal data class EntityUpdateSingleReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : EntityUpdateReturningQuery<Pair<A?, B?>?> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateReturningQuery<Pair<A?, B?>?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleReturningPairColumnsQuery(context, entity, expressions)
    }
}

internal data class EntityUpdateSingleReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : EntityUpdateReturningQuery<Triple<A?, B?, C?>?> {
    override fun options(configure: (UpdateOptions) -> UpdateOptions): EntityUpdateReturningQuery<Triple<A?, B?, C?>?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpdateSingleReturningTripleColumnsQuery(context, entity, expressions)
    }
}
