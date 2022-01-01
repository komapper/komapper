package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor

/**
 * Represents the query whose result can be [Flow].
 * @param T the element type of [Flow]
 */
@ThreadSafe
interface FlowQuery<out T> {
    /**
     * Accepts a visitor.
     * @param VISIT_RESULT the result type
     * @param visitor the visitor
     */
    fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

/**
 * Represents the subquery.
 * @param T the element type of [Flow]
 */
interface FlowSubquery<T> : Subquery<T>, FlowQuery<T> {
    override infix fun except(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
    override infix fun intersect(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
    override infix fun union(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
    override infix fun unionAll(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
}

/**
 * Represents the set operation query.
 * @param T the element type of [Flow]
 */
interface FlowSetOperationQuery<T> : SetOperationQuery<T>, FlowSubquery<T> {
    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<T>
    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<T>
    override fun options(configure: (SelectOptions) -> SelectOptions): FlowSetOperationQuery<T>
}
