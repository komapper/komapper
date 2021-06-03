package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.core.dsl.option.SqlSetOperationOption

data class SqlTripleColumnsSetOperationQuery<A : Any, B : Any, C : Any>(
    val context: SqlSetOperationContext<Triple<A?, B?, C?>>,
    val option: SqlSetOperationOption = SqlSetOperationOption.default,
    val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>
) : SetOperationQuery<Triple<A?, B?, C?>> {

    override val subqueryContext: SubqueryContext<Triple<A?, B?, C?>>
        get() = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<Triple<A?, B?, C?>> = SqlSetOperationQuerySupport(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    override fun first(): Query<Triple<A?, B?, C?>> {
        return Collect(context, option, expressions) { it.first()}
    }

    override fun firstOrNull(): Query<Triple<A?, B?, C?>?> {
        return Collect(context, option, expressions) { it.firstOrNull()}
    }

    override fun <R> collect(transform: (Sequence<Triple<A?, B?, C?>>) -> R): Query<R> {
        return Collect(context, option, expressions, transform)
    }


    override fun except(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<Triple<A?, B?, C?>>): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SetOperationQuery<Triple<A?, B?, C?>> {
        return copy(option = configurator(option))
    }

    class Collect<A : Any, B : Any, C : Any, R>(
        val context: SqlSetOperationContext<Triple<A?, B?, C?>>,
        val option: SqlSetOperationOption,
        val expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>,
        val transform: (Sequence<Triple<A?, B?, C?>>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }

}