package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.expression.ColumnExpression
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

internal data class EntityUpsertSingleReturningQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, R>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val collect: suspend (Flow<ENTITY>) -> R,
) : EntityUpsertReturningQuery<R> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<R> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleReturningQuery(context, entity, collect)
    }
}

internal data class EntityUpsertSingleReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, R>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expression: ColumnExpression<A, *>,
    private val collect: suspend (Flow<A?>) -> R,
) : EntityUpsertReturningQuery<R> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<R> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleReturningSingleColumnQuery(context, entity, expression, collect)
    }
}

internal data class EntityUpsertSingleReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, R>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
    private val collect: suspend (Flow<Pair<A?, B?>>) -> R,
) : EntityUpsertReturningQuery<R> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<R> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleReturningPairColumnsQuery(context, entity, expressions, collect)
    }
}

internal data class EntityUpsertSingleReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any, R>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
    private val collect: suspend (Flow<Triple<A?, B?, C?>>) -> R,
) : EntityUpsertReturningQuery<R> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<R> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertSingleReturningTripleColumnsQuery(context, entity, expressions, collect)
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

internal data class EntityUpsertMultipleReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val expression: ColumnExpression<A, *>,
) : EntityUpsertReturningQuery<List<A?>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<List<A?>> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleReturningSingleColumnQuery(context, entities, expression)
    }
}

internal data class EntityUpsertMultipleReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : EntityUpsertReturningQuery<List<Pair<A?, B?>>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<List<Pair<A?, B?>>> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleReturningPairColumnsQuery(context, entities, expressions)
    }
}

internal data class EntityUpsertMultipleReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : EntityUpsertReturningQuery<List<Triple<A?, B?, C?>>> {

    override fun options(configure: (InsertOptions) -> InsertOptions): EntityUpsertReturningQuery<List<Triple<A?, B?, C?>>> {
        return copy(context = context.copyConfigure(configure))
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityUpsertMultipleReturningTripleColumnsQuery(context, entities, expressions)
    }
}
