package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext

internal class FlowableSubquerySupport<T>(
    private val subqueryContext: SubqueryContext<T>,
    private val transform: (SqlSetOperationContext<T>) -> FlowableSetOperationQuery<T>,
) {

    fun except(other: Subquery<T>): FlowableSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    fun intersect(other: Subquery<T>): FlowableSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    fun union(other: Subquery<T>): FlowableSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    fun unionAll(other: Subquery<T>): FlowableSetOperationQuery<T> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: Subquery<T>
    ): FlowableSetOperationQuery<T> {
        val setOperatorContext = SqlSetOperationContext(kind, this.subqueryContext, other.subqueryContext)
        return transform(setOperatorContext)
    }
}
