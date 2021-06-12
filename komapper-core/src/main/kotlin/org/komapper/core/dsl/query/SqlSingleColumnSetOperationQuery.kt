package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlSingleColumnSetOperationQuery<A : Any>(
    private val context: SqlSetOperationContext<A?>,
    private val option: SqlSetOperationOption = SqlSetOperationOption.default,
    private val expression: ColumnExpression<A, *>
) : FlowableSetOperationQuery<A?> {

    private val support: SqlSetOperationQuerySupport<A?> = SqlSetOperationQuerySupport(context)

    override val subqueryContext: SubqueryContext<A?> = SubqueryContext.SqlSetOperation(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlSingleColumnSetOperationQuery(context, option, expression) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = Query { visitor ->
        visitor.sqlSingleColumnSetOperationQuery(context, option, expression, collect)
    }

    override fun asFlowQuery(): FlowQuery<A?> = FlowQuery { visitor ->
        visitor.sqlSingleColumnSetOperationQuery(context, option, expression)
    }

    override fun except(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<A?>): FlowableSetOperationQuery<A?> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<A?> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<A?> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): FlowableSetOperationQuery<A?> {
        return copy(option = configurator(option))
    }
}
