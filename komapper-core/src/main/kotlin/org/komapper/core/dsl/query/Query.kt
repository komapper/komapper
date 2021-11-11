package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.QueryVisitor

@ThreadSafe
interface Query<T> {
    fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

interface ListQuery<T> : Query<List<T>> {
    fun first(): Query<T> = collect { it.first() }
    fun firstOrNull(): Query<T?> = collect { it.firstOrNull() }
    fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R>
}

interface Subquery<T> : ListQuery<T>, SubqueryExpression<T> {
    infix fun except(other: SubqueryExpression<T>): SetOperationQuery<T>
    infix fun intersect(other: SubqueryExpression<T>): SetOperationQuery<T>
    infix fun union(other: SubqueryExpression<T>): SetOperationQuery<T>
    infix fun unionAll(other: SubqueryExpression<T>): SetOperationQuery<T>
}

interface SetOperationQuery<T> : Subquery<T> {
    fun orderBy(vararg aliases: CharSequence): SetOperationQuery<T>
    fun orderBy(vararg expressions: SortExpression): SetOperationQuery<T>
    fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): SetOperationQuery<T>
}
