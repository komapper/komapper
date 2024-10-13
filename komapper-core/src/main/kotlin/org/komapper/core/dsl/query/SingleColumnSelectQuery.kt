package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SingleColumnSelectQuery<A : Any>(
    override val context: SelectContext<*, *, *>,
    private val expression: ColumnExpression<A, *>,
) : FlowSubquery<A?> {
    private val support: FlowSubquerySupport<A?> =
        FlowSubquerySupport(context) { SingleColumnSetOperationQuery(it, expression = expression) }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.singleColumnSelectQuery(context, expression)
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.singleColumnSelectQuery(context, expression, collect)
        }
    }

    override fun except(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return support.unionAll(other)
    }
}
