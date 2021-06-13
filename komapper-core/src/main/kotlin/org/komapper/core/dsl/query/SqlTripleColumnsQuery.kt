package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlTripleColumnsQuery<A : Any, B : Any, C : Any>(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
) : FlowableSubquery<Triple<A?, B?, C?>> {

    override val subqueryContext: SubqueryContext<Triple<A?, B?, C?>> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<Triple<A?, B?, C?>> =
        FlowableSubquerySupport(subqueryContext) { SqlTripleColumnsSetOperationQuery(it, expressions = expressions) }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlTripleColumnsQuery(context, options, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Triple<A?, B?, C?>>) -> R): Query<R> = Query { visitor ->
        visitor.sqlTripleColumnsQuery(context, options, expressions, collect)
    }

    override fun asFlowQuery(): FlowQuery<Triple<A?, B?, C?>> = FlowQuery { visitor ->
        visitor.sqlTripleColumnsQuery(context, options, expressions)
    }

    override fun except(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return support.unionAll(other)
    }
}
