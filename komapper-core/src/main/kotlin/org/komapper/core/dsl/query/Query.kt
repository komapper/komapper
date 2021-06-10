package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.core.dsl.runner.QueryRunner
import org.komapper.core.dsl.visitor.QueryVisitor

@ThreadSafe
fun interface Query<T> {
    fun accept(visitor: QueryVisitor): QueryRunner

    operator fun <S> plus(other: Query<S>): Query<S> = Query { visitor ->
        visitor.plusQuery(this, other)
    }

    fun <S> flatMap(transform: (T) -> Query<S>): Query<S> = Query { visitor ->
        visitor.flatMapQuery(this, transform)
    }

    fun <S> flatZip(transform: (T) -> Query<S>): Query<Pair<T, S>> = Query { visitor ->
        visitor.flatZipQuery(this, transform)
    }
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T> = collect { it.first() }
    fun firstOrNull(): Query<T?> = collect { it.firstOrNull() }
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
