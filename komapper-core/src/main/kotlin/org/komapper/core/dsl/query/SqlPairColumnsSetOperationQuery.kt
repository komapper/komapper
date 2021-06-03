package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSetOperationOption

data class SqlPairColumnsSetOperationQuery<A : Any, B : Any>(
    val context: SqlSetOperationContext<Pair<A?, B?>>,
    val option: SqlSetOperationOption = SqlSetOperationOption.default,
    val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>
) : SetOperationQuery<Pair<A?, B?>> {

    override val subqueryContext: SubqueryContext<Pair<A?, B?>>
        get() = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Pair<A?, B?>> = SqlSetOperationQuerySupport(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    override fun first(): Query<Pair<A?, B?>> {
        return Collect(context, option, expressions) { it.first() }
    }

    override fun firstOrNull(): Query<Pair<A?, B?>?> {
        return Collect(context, option, expressions) { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<Pair<A?, B?>>) -> R): Query<R> {
        return Collect(context, option, expressions, transform)
    }

    override fun except(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Pair<A?, B?>>): SetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SetOperationQuery<Pair<A?, B?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SetOperationQuery<Pair<A?, B?>> {
        return copy(option = configurator(option))
    }

    class Collect<A : Any, B : Any, R>(
        val context: SqlSetOperationContext<Pair<A?, B?>>,
        val option: SqlSetOperationOption,
        val expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>,
        val transform: (Sequence<Pair<A?, B?>>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }

}