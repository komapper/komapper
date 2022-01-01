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
     * @return the result of visitor processing
     */
    fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

/**
 * Represents the query whose result type is [List].
 * @param T the element type of [List]
 */
interface ListQuery<out T> : Query<List<T>> {
    /**
     * Builds a query that reduces the result set.
     * @param R the result type of collecting
     * @param collect the function to collect a [Flow] instance that represents the result set
     * @return the query that returns a collected value
     */
    fun <R> collect(collect: suspend (Flow<T>) -> R): Query<R>

    /**
     * Accepts a visitor.
     * @param VISIT_RESULT the result type
     * @param visitor the visitor
     * @return the result of visitor processing
     */
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return collect { it.toList() }.accept(visitor)
    }
}

/**
 * Represents the subquery.
 * @param T the element type of [List]
 */
interface Subquery<T> : ListQuery<T>, SubqueryExpression<T> {
    /**
     * Applies the EXCEPT operator.
     * @param other the other subquery
     * @return the query
     */
    infix fun except(other: SubqueryExpression<T>): SetOperationQuery<T>

    /**
     * Applies the INTERSECT operator.
     * @param other the other subquery
     * @return the query
     */
    infix fun intersect(other: SubqueryExpression<T>): SetOperationQuery<T>

    /**
     * Applies the UNION operator.
     * @param other the other subquery
     * @return the query
     */
    infix fun union(other: SubqueryExpression<T>): SetOperationQuery<T>

    /**
     * Applies the UNION ALL operator.
     * @param other the other subquery
     * @return the query
     */
    infix fun unionAll(other: SubqueryExpression<T>): SetOperationQuery<T>
}

/**
 * Represents the set operation query.
 * @param T the element type of [List]
 */
interface SetOperationQuery<T> : Subquery<T> {
    /**
     * Builds an ORDER BY clause.
     * @param aliases the aliases of the columns
     * @return the query
     */
    fun orderBy(vararg aliases: CharSequence): SetOperationQuery<T>

    /**
     * Builds an ORDER BY clause.
     * @param expressions the sort expressions of the columns
     * @return the query
     */
    fun orderBy(vararg expressions: SortExpression): SetOperationQuery<T>

    /**
     * Builds a query with the options applied.
     * @param configure the configure function to apply options
     * @return the query
     */
    fun options(configure: (SelectOptions) -> SelectOptions): SetOperationQuery<T>
}
