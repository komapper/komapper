package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlSingleColumnQuery<A : Any>(
    private val context: SqlSelectContext<*, *, *>,
    private val option: SqlSelectOption,
    private val expression: ColumnExpression<A, *>
) : FlowableSubquery<A?> {

    override val subqueryContext: SubqueryContext<A?> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<A?> =
        FlowableSubquerySupport(subqueryContext) { SqlSingleColumnSetOperationQuery(it, expression = expression) }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlSingleColumnQuery(context, option, expression) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = Query { visitor ->
        visitor.sqlSingleColumnQuery(context, option, expression, collect)
    }

    override fun asFlowQuery(): FlowQuery<A?> = FlowQuery { visitor ->
        visitor.sqlSingleColumnQuery(context, option, expression)
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
