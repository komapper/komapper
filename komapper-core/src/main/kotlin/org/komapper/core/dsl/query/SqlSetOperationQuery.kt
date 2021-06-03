package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSetOperationOption

interface SqlSetOperationQuery<T : Any> : SetOperationQuery<T>

data class SqlSetOperationQueryImpl<T : Any>(
    val context: SqlSetOperationContext<T>,
    val option: SqlSetOperationOption = SqlSetOperationOption.default,
    val metamodel: EntityMetamodel<T, *, *>
) : SqlSetOperationQuery<T> {

    override val subqueryContext = SubqueryContext.SqlSetOperation(context)

    private val support: SqlSetOperationQuerySupport<T> = SqlSetOperationQuerySupport(context)

    override fun first(): Query<T> {
        return Collect(context, option, metamodel) { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Collect(context, option, metamodel) { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<T>) -> R): Query<R> {
        return Collect(context, option, metamodel, transform)
    }

    override fun except(other: Subquery<T>): SqlSetOperationQuery<T> {
        return copy(context = support.except(other))
    }

    override fun intersect(other: Subquery<T>): SqlSetOperationQuery<T> {
        return copy(context = support.intersect(other))
    }

    override fun union(other: Subquery<T>): SqlSetOperationQuery<T> {
        return copy(context = support.union(other))
    }

    override fun unionAll(other: Subquery<T>): SqlSetOperationQuery<T> {
        return copy(context = support.unionAll(other))
    }

    override fun orderBy(vararg aliases: CharSequence): SqlSetOperationQuery<T> {
        return copy(context = support.orderBy(*aliases))
    }

    override fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSetOperationQuery<T> {
        return copy(context = support.orderBy(*expressions))
    }

    override fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SqlSetOperationQuery<T> {
        return copy(option = configurator(option))
    }


    override fun accept(visitor: QueryVisitor): QueryRunner {
        return visitor.visit(this)
    }


    class Collect<T : Any, R>(
        val context: SqlSetOperationContext<T>,
        val option: SqlSetOperationOption,
        val metamodel: EntityMetamodel<T, *, *>,
        val transform: (Sequence<T>) -> R
    ) : Query<R> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }

}
