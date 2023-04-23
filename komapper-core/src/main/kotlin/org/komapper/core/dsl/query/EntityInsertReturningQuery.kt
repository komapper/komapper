package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.expression.ColumnExpression
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

internal data class EntityInsertSingleReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expression: ColumnExpression<A, *>,
) : EntityInsertReturningQuery<A?> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<A?> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleReturningSingleColumnQuery(context, entity, expression)
    }
}

internal data class EntityInsertSingleReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : EntityInsertReturningQuery<Pair<A?, B?>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<Pair<A?, B?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleReturningPairColumnsQuery(context, entity, expressions)
    }
}

internal data class EntityInsertSingleReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : EntityInsertReturningQuery<Triple<A?, B?, C?>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<Triple<A?, B?, C?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertSingleReturningTripleColumnsQuery(context, entity, expressions)
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

internal data class EntityInsertMultipleReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val expression: ColumnExpression<A, *>,
) : EntityInsertReturningQuery<List<A?>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<List<A?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleReturningSingleColumnQuery(context, entities, expression)
    }
}

internal data class EntityInsertMultipleReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : EntityInsertReturningQuery<List<Pair<A?, B?>>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<List<Pair<A?, B?>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleReturningPairColumnsQuery(context, entities, expressions)
    }
}

internal data class EntityInsertMultipleReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : EntityInsertReturningQuery<List<Triple<A?, B?, C?>>> {
    override fun options(configure: (InsertOptions) -> InsertOptions): EntityInsertReturningQuery<List<Triple<A?, B?, C?>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.entityInsertMultipleReturningTripleColumnsQuery(context, entities, expressions)
    }
}
