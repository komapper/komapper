package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class TripleNotNullColumnsSetOperationQuery<A : Any, B : Any, C : Any>(
    override val context: SetOperationContext,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
) : FlowSetOperationQuery<Triple<A, B, C>> {

    private val support: SetOperationQuerySupport<Triple<A, B, C>> = SetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.tripleNotNullColumnsSetOperationQuery(context, expressions) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.tripleNotNullColumnsSetOperationQuery(context, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Triple<A, B, C>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.tripleNotNullColumnsSetOperationQuery(context, expressions, collect)
        }
    }

    override fun except(other: SubqueryExpression<Triple<A, B, C>>): FlowSetOperationQuery<Triple<A, B, C>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: SubqueryExpression<Triple<A, B, C>>): FlowSetOperationQuery<Triple<A, B, C>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: SubqueryExpression<Triple<A, B, C>>): FlowSetOperationQuery<Triple<A, B, C>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: SubqueryExpression<Triple<A, B, C>>): FlowSetOperationQuery<Triple<A, B, C>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<Triple<A, B, C>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<Triple<A, B, C>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configure: (SelectOptions) -> SelectOptions): FlowSetOperationQuery<Triple<A, B, C>> {
        val newContext = context.copy(options = configure(context.options))
        return copy(context = newContext)
    }
}
