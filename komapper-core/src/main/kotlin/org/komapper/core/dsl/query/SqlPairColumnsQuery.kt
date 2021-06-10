package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlPairColumnsQuery<A : Any, B : Any>(
    private val context: SqlSelectContext<*, *, *>,
    private val option: SqlSelectOption,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : FlowableSubquery<Pair<A?, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A?, B?>> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<Pair<A?, B?>> =
        FlowableSubquerySupport(subqueryContext) { SqlPairColumnsSetOperationQuery(it, expressions = expressions) }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlPairColumnsQuery(context, option, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A?, B?>>) -> R): Query<R> = Query { visitor ->
        visitor.sqlPairColumnsQuery(context, option, expressions, collect)
    }

    override fun asFlowQuery(): FlowQuery<Pair<A?, B?>> = FlowQuery { visitor ->
        visitor.sqlPairColumnsQuery(context, option, expressions)
    }

    override fun except(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return support.unionAll(other)
    }
}
