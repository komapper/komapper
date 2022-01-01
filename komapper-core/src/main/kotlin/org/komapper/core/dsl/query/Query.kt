package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.QueryVisitor

/**
 * Represents the query to database.
 * @param T the result type of query execution
 */
@ThreadSafe
interface Query<out T> {
    /**
     * Accepts a visitor.
     * @param VISIT_RESULT the result type
     * @param visitor the visitor
     */
    fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

/**
 * Represents the query whose result type is [List].
 * @param T the element type of [List]
 */
interface ListQuery<out T> : Query<List<T>> {
    fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R>
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return collect { it.toList() }.accept(visitor)
    }
}

/**
 * Represents the subquery.
 * @param T the element type of [List]
 */
interface Subquery<T> : ListQuery<T>, SubqueryExpression<T> {
    infix fun except(other: SubqueryExpression<T>): SetOperationQuery<T>
    infix fun intersect(other: SubqueryExpression<T>): SetOperationQuery<T>
    infix fun union(other: SubqueryExpression<T>): SetOperationQuery<T>
    infix fun unionAll(other: SubqueryExpression<T>): SetOperationQuery<T>
}

/**
 * Represents the set operation query.
 * @param T the element type of [List]
 */
interface SetOperationQuery<T> : Subquery<T> {
    fun orderBy(vararg aliases: CharSequence): SetOperationQuery<T>
    fun orderBy(vararg expressions: SortExpression): SetOperationQuery<T>
    fun options(configure: (SelectOptions) -> SelectOptions): SetOperationQuery<T>
}
