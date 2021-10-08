package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlSingleColumnSetOperationQuery<A : Any>(
    override val context: SqlSetOperationContext,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val expression: ColumnExpression<A, *>
) : FlowSetOperationQuery<A?> {

    private val support: SqlSetOperationQuerySupport<A?> = SqlSetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSingleColumnSetOperationQuery(context, options, expression) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSingleColumnSetOperationQuery(context, options, expression)
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSingleColumnSetOperationQuery(context, options, expression, collect)
        }
    }

    override fun except(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: SubqueryExpression<A?>): FlowSetOperationQuery<A?> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<A?> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<A?> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowSetOperationQuery<A?> {
        return copy(options = configurator(options))
    }
}
