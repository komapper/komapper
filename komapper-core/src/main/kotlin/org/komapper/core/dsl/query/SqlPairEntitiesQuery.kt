package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

internal class SqlPairEntitiesQuery<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>(
    private val context: SqlSelectContext<A, *, A_META>,
    private val options: SqlSelectOptions,
    private val metamodels: Pair<A_META, B_META>
) : FlowableSubquery<Pair<A, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A, B?>> = SubqueryContext.SqlSelect(context)

    private val support: FlowableSubquerySupport<Pair<A, B?>> =
        FlowableSubquerySupport(subqueryContext) { SqlPairEntitiesSetOperationQuery(it, metamodels = metamodels) }

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.sqlPairEntitiesQuery(context, options, metamodels) { it.toList() }
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A, B?>>) -> R): Query<R> = Query { visitor ->
        visitor.sqlPairEntitiesQuery(context, options, metamodels, collect)
    }

    override fun asFlowQuery(): FlowQuery<Pair<A, B?>> = FlowQuery { visitor ->
        visitor.sqlPairEntitiesQuery(context, options, metamodels)
    }

    override fun except(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return support.except(other)
    }

    override fun intersect(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return support.intersect(other)
    }

    override fun union(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return support.union(other)
    }

    override fun unionAll(other: Subquery<Pair<A, B?>>): FlowableSetOperationQuery<Pair<A, B?>> {
        return support.unionAll(other)
    }
}
