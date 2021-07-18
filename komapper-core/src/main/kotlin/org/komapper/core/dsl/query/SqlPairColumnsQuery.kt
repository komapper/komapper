package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlPairColumnsQuery<A : Any, B : Any>(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : FlowableSubquery<Pair<A?, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A?, B?>> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<Pair<A?, B?>> =
        FlowableSubquerySupport(subqueryContext) { SqlPairColumnsSetOperationQuery(it, expressions = expressions) }

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlPairColumnsQuery(context, options, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A?, B?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlPairColumnsQuery(context, options, expressions, collect)
        }
    }

    override fun asFlowQuery(): FlowQuery<Pair<A?, B?>> = object : FlowQuery<Pair<A?, B?>> {
        override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlPairColumnsQuery(context, options, expressions)
        }
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
