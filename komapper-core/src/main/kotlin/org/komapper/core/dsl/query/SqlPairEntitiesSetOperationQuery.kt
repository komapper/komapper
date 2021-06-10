package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlPairEntitiesSetOperationQuery<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>(
    private val context: SqlSetOperationContext<Pair<A, B?>>,
    private val option: SqlSetOperationOption = SqlSetOperationOption.default,
    private val metamodels: Pair<A_META, B_META>
) : FlowableSetOperationQuery<Pair<A, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A, B?>> = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Pair<A, B?>> = SqlSetOperationQuerySupport(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlPairEntitiesSetOperationQuery(context, option, metamodels) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A, B?>>) -> R): Query<R> = Query { visitor ->
        visitor.sqlPairEntitiesSetOperationQuery(context, option, metamodels, collect)
    }

    override fun asFlowQuery(): FlowQuery<Pair<A, B?>> = FlowQuery { visitor ->
        visitor.sqlPairEntitiesSetOperationQuery(context, option, metamodels)
    }

    override fun except(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): FlowableSetOperationQuery<Pair<A, B?>> {
        return copy(option = configurator(option))
    }
}
