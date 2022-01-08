package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor

/**
 * Represents the query whose result is [Flow].
 *
 * @param T the element type of [Flow]
 */
@ThreadSafe
interface FlowQuery<out T> {
    /**
     * Accepts a visitor.
     *
     * @param VISIT_RESULT the result type
     * @param visitor the visitor
     */
    fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

/**
 * Represents a subquery.
 *
 * @param T the element type of [Flow]
 */
interface FlowSubquery<T> : Subquery<T>, FlowQuery<T> {
    /**
     * Builds a query with an EXCEPT operator.
     *
     * @param other the other subquery
     * @return the query
     */
    override infix fun except(other: SubqueryExpression<T>): FlowSetOperationQuery<T>

    /**
     * Builds a query with an INTERSECT operator.
     *
     * @param other the other subquery
     * @return the query
     */
    override infix fun intersect(other: SubqueryExpression<T>): FlowSetOperationQuery<T>

    /**
     * Builds a query with a UNION operator.
     *
     * @param other the other subquery
     * @return the query
     */
    override infix fun union(other: SubqueryExpression<T>): FlowSetOperationQuery<T>

    /**
     * Builds a query with a UNION ALL operator.
     *
     * @param other the other subquery
     * @return the query
     */
    override infix fun unionAll(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
}

/**
 * Represents the set operation query.
 *
 * @param T the element type of [Flow]
 */
interface FlowSetOperationQuery<T> : SetOperationQuery<T>, FlowSubquery<T> {
    /**
     * Builds a query with an ORDER BY clause.
     *
     * @param aliases the aliases of the columns
     * @return the query
     */
    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<T>

    /**
     * Builds a query with an ORDER BY clause.
     *
     * @param expressions the sort expressions of the columns
     * @return the query
     */
    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<T>

    /**
     * Builds a query with the options applied.
     *
     * @param configure the configure function to apply options
     * @return the query
     */
    override fun options(configure: (SelectOptions) -> SelectOptions): FlowSetOperationQuery<T>
}
