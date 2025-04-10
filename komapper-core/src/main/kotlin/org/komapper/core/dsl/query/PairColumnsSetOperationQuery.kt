package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class PairColumnsSetOperationQuery<A : Any, B : Any>(
    override val context: SetOperationContext,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
) : FlowSetOperationQuery<Pair<A?, B?>> {
    private val support: SetOperationQuerySupport<Pair<A?, B?>> = SetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.pairColumnsSetOperationQuery(context, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A?, B?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.pairColumnsSetOperationQuery(context, expressions, collect)
        }
    }

    override fun except(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): FlowSetOperationQuery<Pair<A?, B?>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }
}
