package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.SubqueryExpression

internal data class SqlSetOperationQuerySupport<T : Any?>(
    private val context: SqlSetOperationContext
) {

    fun except(other: SubqueryExpression<T>): SqlSetOperationContext {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    fun intersect(other: SubqueryExpression<T>): SqlSetOperationContext {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    fun union(other: SubqueryExpression<T>): SqlSetOperationContext {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    fun unionAll(other: SubqueryExpression<T>): SqlSetOperationContext {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(kind: SqlSetOperationKind, other: SubqueryExpression<T>): SqlSetOperationContext {
        return context.copy(
            kind = kind,
            left = context,
            right = other.context
        )
    }

    fun orderBy(vararg aliases: CharSequence): SqlSetOperationContext {
        val items = aliases.map {
            if (it is SortItem) it else SortItem.Alias.Asc(it.toString())
        }
        return orderBy(items)
    }

    fun orderBy(vararg expressions: SortExpression): SqlSetOperationContext {
        val items = expressions.map(SortItem.Column::of)
        return orderBy(items)
    }

    private fun orderBy(items: List<SortItem>): SqlSetOperationContext {
        return context.copy(orderBy = context.orderBy + items)
    }
}
