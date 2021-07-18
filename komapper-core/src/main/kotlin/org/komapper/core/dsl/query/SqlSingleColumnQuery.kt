package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlSingleColumnQuery<A : Any>(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val expression: ColumnExpression<A, *>
) : FlowableSubquery<A?> {

    override val subqueryContext: SubqueryContext<A?> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<A?> =
        FlowableSubquerySupport(subqueryContext) { SqlSingleColumnSetOperationQuery(it, expression = expression) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSingleColumnQuery(context, options, expression) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSingleColumnQuery(context, options, expression, collect)
        }
    }

    override fun asFlowQuery(): FlowQuery<A?> = object : FlowQuery<A?> {
        override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSingleColumnQuery(context, options, expression)
        }
    }

    override fun except(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return support.unionAll(other)
    }
}
