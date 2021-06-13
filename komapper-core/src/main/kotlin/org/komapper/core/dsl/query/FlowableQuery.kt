package org.komapper.core.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.options.SqlSetOperationOptions

@ThreadSafe
interface FlowableQuery<T> {
    fun asFlowQuery(): FlowQuery<T>
}

interface FlowableSubquery<T> : Subquery<T>, FlowableQuery<T> {
    override infix fun except(other: Subquery<T>): FlowableSetOperationQuery<T>
    override infix fun intersect(other: Subquery<T>): FlowableSetOperationQuery<T>
    override infix fun union(other: Subquery<T>): FlowableSetOperationQuery<T>
    override infix fun unionAll(other: Subquery<T>): FlowableSetOperationQuery<T>
}

interface FlowableSetOperationQuery<T> : SetOperationQuery<T>, FlowableSubquery<T> {
    override fun orderBy(vararg aliases: CharSequence): FlowableSetOperationQuery<T>
    override fun orderBy(vararg expressions: ColumnExpression<*, *>): FlowableSetOperationQuery<T>
    override fun options(configurator: (SqlSetOperationOptions) -> SqlSetOperationOptions): FlowableSetOperationQuery<T>
}
