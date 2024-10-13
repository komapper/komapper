package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SubqueryExpression

internal class FlowSubquerySupport<T>(
    private val context: SubqueryContext,
    private val transform: (SetOperationContext) -> FlowSetOperationQuery<T>,
) {
    fun except(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SetOperationKind.EXCEPT, other)
    }

    fun intersect(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SetOperationKind.INTERSECT, other)
    }

    fun union(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SetOperationKind.UNION, other)
    }

    fun unionAll(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SetOperationKind,
        other: SubqueryExpression<T>,
    ): FlowSetOperationQuery<T> {
        val setOperatorContext = SetOperationContext(kind, context, other.context, options = context.options)
        return transform(setOperatorContext)
    }
}
