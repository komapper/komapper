package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents a query to delete rows and retrieve the deleted values.
 *
 * @param T the result type
 */
interface RelationDeleteReturningQuery<T> : Query<T> {
    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteReturningQuery<T>
}

internal data class RelationDeleteReturningQueryImpl<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : RelationDeleteReturningQuery<List<ENTITY>> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteReturningQuery<List<ENTITY>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationDeleteReturningQuery(context)
    }
}

internal data class RelationDeleteReturningSingleColumnQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
    private val expression: ColumnExpression<A, *>,
) : RelationDeleteReturningQuery<List<A?>> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteReturningQuery<List<A?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationDeleteReturningSingleColumnQuery(context, expression)
    }
}

internal data class RelationDeleteReturningPairColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : RelationDeleteReturningQuery<List<Pair<A?, B?>>> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteReturningQuery<List<Pair<A?, B?>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationDeleteReturningPairColumnsQuery(context, expressions)
    }
}

internal data class RelationDeleteReturningTripleColumnsQuery<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, A : Any, B : Any, C : Any>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
) : RelationDeleteReturningQuery<List<Triple<A?, B?, C?>>> {
    override fun options(configure: (DeleteOptions) -> DeleteOptions): RelationDeleteReturningQuery<List<Triple<A?, B?, C?>>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.relationDeleteReturningTripleColumnsQuery(context, expressions)
    }
}
