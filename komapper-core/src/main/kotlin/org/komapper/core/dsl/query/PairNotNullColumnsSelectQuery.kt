package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class PairNotNullColumnsSelectQuery<A : Any, B : Any>(
    override val context: SelectContext<*, *, *>,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : FlowSubquery<Pair<A, B>> {

    private val support: FlowSubquerySupport<Pair<A, B>> =
        FlowSubquerySupport(context) { PairNotNullColumnsSetOperationQuery(it, expressions = expressions) }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.pairNotNullColumnsSelectQuery(context, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A, B>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.pairNotNullColumnsSelectQuery(context, expressions, collect)
        }
    }

    override fun except(other: SubqueryExpression<Pair<A, B>>): FlowSetOperationQuery<Pair<A, B>> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<Pair<A, B>>): FlowSetOperationQuery<Pair<A, B>> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<Pair<A, B>>): FlowSetOperationQuery<Pair<A, B>> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<Pair<A, B>>): FlowSetOperationQuery<Pair<A, B>> {
        return support.unionAll(other)
    }
}
