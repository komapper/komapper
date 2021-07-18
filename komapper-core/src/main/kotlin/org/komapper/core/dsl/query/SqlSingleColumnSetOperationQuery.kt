package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlSingleColumnSetOperationQuery<A : Any>(
    private val context: SqlSetOperationContext<A?>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val expression: ColumnExpression<A, *>
) : FlowableSetOperationQuery<A?> {

    private val support: SqlSetOperationQuerySupport<A?> = SqlSetOperationQuerySupport(context)

    override val subqueryContext: SubqueryContext<A?> = SubqueryContext.SqlSetOperation(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlSingleColumnSetOperationQuery(context, options, expression) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSingleColumnSetOperationQuery(context, options, expression, collect)
        }
    }

    override fun asFlowQuery(): FlowQuery<A?> = object : FlowQuery<A?> {
        override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlSingleColumnSetOperationQuery(context, options, expression)
        }
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

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowableSetOperationQuery<A?> {
        return copy(options = configurator(options))
    }
}
