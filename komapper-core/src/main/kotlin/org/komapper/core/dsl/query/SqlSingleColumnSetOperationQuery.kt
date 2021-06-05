package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.option.SqlSetOperationOption

data class SqlSingleColumnSetOperationQuery<A : Any>(
    val context: SqlSetOperationContext<A?>,
    val option: SqlSetOperationOption = SqlSetOperationOption.default,
    val expression: ColumnExpression<A, *>
) : SetOperationQuery<A?> {

    override val subqueryContext: SubqueryContext<A?>
        get() = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<A?> = SqlSetOperationQuerySupport(context)

    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }

    override fun first(): Query<A?> {
        return Collect(context, option, expression) { it.first() }
    }

    override fun firstOrNull(): Query<A?> {
        return Collect(context, option, expression) { it.firstOrNull() }
    }

    override fun <R> collect(collect: suspend (Flow<A?>) -> R): Query<R> {
        return Collect(context, option, expression, collect)
    }

    override fun except(other: Subquery<A?>): SetOperationQuery<A?> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<A?>): SetOperationQuery<A?> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<A?>): SetOperationQuery<A?> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<A?>): SetOperationQuery<A?> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SetOperationQuery<A?> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SetOperationQuery<A?> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SetOperationQuery<A?> {
        return copy(option = configurator(option))
    }

    class Collect<A : Any, R>(
        val context: SqlSetOperationContext<A?>,
        val option: SqlSetOperationOption,
        val expression: ColumnExpression<A, *>,
        val transform: suspend (Flow<A?>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}
