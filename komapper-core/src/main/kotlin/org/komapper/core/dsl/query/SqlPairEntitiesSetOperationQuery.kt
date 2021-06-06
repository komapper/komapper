package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSetOperationOption

data class SqlPairEntitiesSetOperationQuery<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>>(
    val context: SqlSetOperationContext<Pair<A, B?>>,
    val option: SqlSetOperationOption = SqlSetOperationOption.default,
    val metamodels: Pair<A_META, B_META>
) : SetOperationQuery<Pair<A, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A, B?>>
        get() = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Pair<A, B?>> = SqlSetOperationQuerySupport(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    override fun first(): Query<Pair<A, B?>> {
        return Collect(context, option, metamodels) { it.first() }
    }

    override fun firstOrNull(): Query<Pair<A, B?>?> {
        return Collect(context, option, metamodels) { it.firstOrNull() }
    }

    override fun <R> collect(collect: suspend (Flow<Pair<A, B?>>) -> R): Query<R> {
        return Collect(context, option, metamodels, collect)
    }

    override fun except(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Pair<A, B?>>): SetOperationQuery<Pair<A, B?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SetOperationQuery<Pair<A, B?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SetOperationQuery<Pair<A, B?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SetOperationQuery<Pair<A, B?>> {
        return copy(option = configurator(option))
    }

    class Collect<A : Any, A_META : EntityMetamodel<A, *, A_META>, B : Any, B_META : EntityMetamodel<B, *, B_META>, R>(
        val context: SqlSetOperationContext<Pair<A, B?>>,
        val option: SqlSetOperationOption,
        val metamodels: Pair<A_META, B_META>,
        val transform: suspend (Flow<Pair<A, B?>>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}
