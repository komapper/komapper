package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlPairColumnsSetOperationQuery<A : Any, B : Any>(
    private val context: SqlSetOperationContext<Pair<A?, B?>>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : FlowableSetOperationQuery<Pair<A?, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A?, B?>> = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Pair<A?, B?>> = SqlSetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlPairColumnsSetOperationQuery(context, options, expressions) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A?, B?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlPairColumnsSetOperationQuery(context, options, expressions, collect)
        }
    }

    override fun asFlowQuery(): FlowQuery<Pair<A?, B?>> = object : FlowQuery<Pair<A?, B?>> {
        override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlPairColumnsSetOperationQuery(context, options, expressions)
        }
    }

    override fun except(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Pair<A?, B?>>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowableSetOperationQuery<Pair<A?, B?>> {
        return copy(options = configurator(options))
    }
}
