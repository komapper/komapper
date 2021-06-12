package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlMultipleColumnsSetOperationQuery(
    private val context: SqlSetOperationContext<Columns>,
    private val option: SqlSetOperationOption = SqlSetOperationOption.default,
    private val expressions: List<ColumnExpression<*, *>>
) : FlowableSetOperationQuery<Columns> {

    private val support: SqlSetOperationQuerySupport<Columns> = SqlSetOperationQuerySupport(context)

    override val subqueryContext: SubqueryContext<Columns> = SubqueryContext.SqlSetOperation(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlMultipleColumnsSetOperationQuery(context, option, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Columns>) -> R): Query<R> = Query { visitor ->
        visitor.sqlMultipleColumnsSetOperationQuery(context, option, expressions, collect)
    }

    override fun asFlowQuery(): FlowQuery<Columns> = FlowQuery { visitor ->
        visitor.sqlMultipleColumnsSetOperationQuery(context, option, expressions)
    }

    override fun except(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Columns>): FlowableSetOperationQuery<Columns> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<Columns> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<Columns> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): FlowableSetOperationQuery<Columns> {
        return copy(option = configurator(option))
    }
}
