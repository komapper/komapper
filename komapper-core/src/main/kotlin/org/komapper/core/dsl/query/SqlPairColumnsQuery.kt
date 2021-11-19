package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlPairColumnsQuery<A : Any, B : Any>(
    override val context: SqlSelectContext<*, *, *>,
    private val options: SelectOptions,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : FlowSubquery<Pair<A?, B?>> {

    private val support: FlowSubquerySupport<Pair<A?, B?>> =
        FlowSubquerySupport(context) { SqlPairColumnsSetOperationQuery(it, expressions = expressions) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlPairColumnsQuery(context, options, expressions) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlPairColumnsQuery(context, options, expressions)
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A?, B?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlPairColumnsQuery(context, options, expressions, collect)
        }
    }

    override fun except(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return support.except(other)
    }

    override fun intersect(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return support.intersect(other)
    }

    override fun union(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return support.union(other)
    }

    override fun unionAll(other: SubqueryExpression<Pair<A?, B?>>): FlowSetOperationQuery<Pair<A?, B?>> {
        return support.unionAll(other)
    }
}
