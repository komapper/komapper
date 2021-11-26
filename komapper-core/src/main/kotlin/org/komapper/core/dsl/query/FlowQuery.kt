package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor

@ThreadSafe
interface FlowQuery<out T> {
    fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

interface FlowSubquery<T> : Subquery<T>, FlowQuery<T> {
    override infix fun except(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
    override infix fun intersect(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
    override infix fun union(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
    override infix fun unionAll(other: SubqueryExpression<T>): FlowSetOperationQuery<T>
}

interface FlowSetOperationQuery<T> : SetOperationQuery<T>, FlowSubquery<T> {
    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<T>
    override fun orderBy(vararg expressions: SortExpression): FlowSetOperationQuery<T>
    override fun options(configurator: (SelectOptions) -> SelectOptions): FlowSetOperationQuery<T>
}
