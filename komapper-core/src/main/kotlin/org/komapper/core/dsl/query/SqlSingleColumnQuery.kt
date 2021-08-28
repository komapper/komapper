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
) : FlowSubquery<A?> {

    override val subqueryContext: SubqueryContext<A?> = SubqueryContext.SqlSelect(context)

    private val support: FlowSubquerySupport<A?> =
        FlowSubquerySupport(subqueryContext) { SqlSingleColumnSetOperationQuery(it, expression = expression) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSingleColumnQuery(context, options, expression) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSingleColumnQuery(context, options, expression)
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSingleColumnQuery(context, options, expression, collect)
        }
    }

    override fun except(other: Subquery<A?>): FlowSetOperationQuery<A?> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<A?>): FlowSetOperationQuery<A?> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<A?>): FlowSetOperationQuery<A?> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<A?>): FlowSetOperationQuery<A?> {
        return support.unionAll(other)
    }
}
