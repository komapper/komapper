package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlMultipleColumnsSetOperationQuery(
    private val context: SqlSetOperationContext<Columns>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val expressions: List<ColumnExpression<*, *>>
) : FlowSetOperationQuery<Columns> {

    private val support: SqlSetOperationQuerySupport<Columns> = SqlSetOperationQuerySupport(context)

    override val subqueryContext: SubqueryContext<Columns> = SubqueryContext.SqlSetOperation(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleColumnsSetOperationQuery(context, options, expressions) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlMultipleColumnsSetOperationQuery(context, options, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Columns>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlMultipleColumnsSetOperationQuery(context, options, expressions, collect)
        }
    }

    override fun except(other: Subquery<Columns>): FlowSetOperationQuery<Columns> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Columns>): FlowSetOperationQuery<Columns> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Columns>): FlowSetOperationQuery<Columns> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Columns>): FlowSetOperationQuery<Columns> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<Columns> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowSetOperationQuery<Columns> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowSetOperationQuery<Columns> {
        return copy(options = configurator(options))
    }
}
