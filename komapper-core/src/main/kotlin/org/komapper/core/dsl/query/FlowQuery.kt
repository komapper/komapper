package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.visitor.FlowQueryVisitor

@ThreadSafe
interface FlowQuery<T> {
    fun <VISIT_RESULT> accept(visitor: FlowQueryVisitor<VISIT_RESULT>): VISIT_RESULT
}

interface FlowSubquery<T> : Subquery<T>, FlowQuery<T> {
    override infix fun except(other: Subquery<T>): FlowSetOperationQuery<T>
    override infix fun intersect(other: Subquery<T>): FlowSetOperationQuery<T>
    override infix fun union(other: Subquery<T>): FlowSetOperationQuery<T>
    override infix fun unionAll(other: Subquery<T>): FlowSetOperationQuery<T>
}

interface FlowSetOperationQuery<T> : SetOperationQuery<T>, FlowSubquery<T> {
    override fun orderBy(vararg aliases: CharSequence): FlowSetOperationQuery<T>
    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowSetOperationQuery<T>
    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowSetOperationQuery<T>
}
