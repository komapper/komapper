package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlTripleColumnsSetOperationQuery<A : Any, B : Any, C : Any>(
    private val context: SqlSetOperationContext<Triple<A?, B?, C?>>,
    private val option: SqlSetOperationOption = SqlSetOperationOption.default,
    private val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
) : FlowableSetOperationQuery<Triple<A?, B?, C?>> {

    private val support: SqlSetOperationQuerySupport<Triple<A?, B?, C?>> = SqlSetOperationQuerySupport(context)

    override val subqueryContext: SubqueryContext<Triple<A?, B?, C?>> = SubqueryContext.SqlSetOperation(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlTripleColumnsSetOperationQuery(context, option, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Triple<A?, B?, C?>>) -> R): Query<R> = Query { visitor ->
        visitor.sqlTripleColumnsSetOperationQuery(context, option, expressions, collect)
    }

    override fun asFlowQuery(): FlowQuery<Triple<A?, B?, C?>> = FlowQuery { visitor ->
        visitor.sqlTripleColumnsSetOperationQuery(context, option, expressions)
    }

    override fun except(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Triple<A?, B?, C?>>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): FlowableSetOperationQuery<Triple<A?, B?, C?>> {
        return copy(option = configurator(option))
    }
}
