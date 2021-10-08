package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.SubqueryExpression

internal class FlowSubquerySupport<T>(
    private val context: SubqueryContext,
    private val transform: (SqlSetOperationContext) -> FlowSetOperationQuery<T>,
) {

    fun except(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    fun intersect(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    fun union(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    fun unionAll(other: SubqueryExpression<T>): FlowSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: SubqueryExpression<T>
    ): FlowSetOperationQuery<T> {
        val setOperatorContext = SqlSetOperationContext(kind, context, other.context)
        return transform(setOperatorContext)
    }
}
