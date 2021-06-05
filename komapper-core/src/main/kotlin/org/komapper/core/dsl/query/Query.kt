package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.option.SqlSetOperationOption

interface Query<T> {
    fun accept(visitor: QueryVisitor): QueryRunner

    operator fun <S> plus(other: Query<S>): Query<S> {
        return Plus(this, other)
    }

    fun <S> flatMap(transform: (T) -> Query<S>): Query<S> {
        return FlatMap(this, transform)
    }

    fun <S> flatZip(transform: (T) -> Query<S>): Query<Pair<T, S>> {
        return FlatZip(this, transform)
    }

    data class Plus<T, S>(val left: Query<T>, val right: Query<S>) : Query<S> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }

    data class FlatMap<T, S>(val query: Query<T>, val transform: (T) -> Query<S>) : Query<S> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }

    data class FlatZip<T, S>(val query: Query<T>, val transform: (T) -> Query<S>) : Query<Pair<T, S>> {
        override fun accept(visitor: QueryVisitor): QueryRunner {
            return visitor.visit(this)
        }
    }
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T>
    fun firstOrNull(): Query<T?>
    fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R>
}

interface Subquery<T> : ListQuery<T>, SubqueryExpression<T> {
    override val subqueryContext: SubqueryContext<T>
    infix fun except(other: Subquery<T>): SetOperationQuery<T>
    infix fun intersect(other: Subquery<T>): SetOperationQuery<T>
    infix fun union(other: Subquery<T>): SetOperationQuery<T>
    infix fun unionAll(other: Subquery<T>): SetOperationQuery<T>
}

interface SetOperationQuery<T> : Subquery<T> {
    fun orderBy(vararg aliases: CharSequence): SetOperationQuery<T>
    fun orderBy(vararg expressions: ColumnExpression<*, *>): SetOperationQuery<T>
    fun option(configurator: (SqlSetOperationOption) -> SqlSetOperationOption): SetOperationQuery<T>
}

