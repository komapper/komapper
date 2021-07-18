package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.QueryVisitor

@ThreadSafe
interface Query<T> {
    fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT

    operator fun <S> plus(other: Query<S>): Query<S> = object : Query<S> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.plusQuery(this@Query, other)
        }
    }

    fun <S> flatMap(transform: (T) -> Query<S>): Query<S> = object : Query<S> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.flatMapQuery(this@Query, transform)
        }
    }

    fun <S> flatZip(transform: (T) -> Query<S>): Query<Pair<T, S>> = object : Query<Pair<T, S>> {
        override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
            return visitor.flatZipQuery(this@Query, transform)
        }
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
    fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): SetOperationQuery<T>
}
