package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor

internal data class SqlPairEntitiesSetOperationQuery<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>(
    private val context: SqlSetOperationContext<Pair<A, B?>>,
    private val options: SqlSetOperationOptions = SqlSetOperationOptions.default,
    private val metamodels: Pair<A_META, B_META>
) : FlowSetOperationQuery<Pair<A, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A, B?>> = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Pair<A, B?>> = SqlSetOperationQuerySupport(context)

    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlPairEntitiesSetOperationQuery(context, options, metamodels) { it.toList() }
    }

    override fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.sqlPairEntitiesSetOperationQuery(context, options, metamodels)
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A, B?>>) -> R): Query<R> = object : Query<R> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.sqlPairEntitiesSetOperationQuery(context, options, metamodels, collect)
        }
    }

    override fun except(other: Subquery<Pair<A, B?>>): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Pair<A, B?>>): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Pair<A, B?>>): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Pair<A, B?>>): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowSetOperationQuery<Pair<A, B?>> {
        return copy(options = configurator(options))
    }
}
