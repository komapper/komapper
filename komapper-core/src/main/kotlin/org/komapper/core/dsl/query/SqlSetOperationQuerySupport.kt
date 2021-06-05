package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.ColumnExpression

data class SqlSetOperationQuerySupport<T : Any?>(
    val context: SqlSetOperationContext<T>
) {

    val subqueryContext = SubqueryContext.SqlSetOperation(context)

    fun except(other: Subquery<T>): SqlSetOperationContext<T> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    fun intersect(other: Subquery<T>): SqlSetOperationContext<T> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    fun union(other: Subquery<T>): SqlSetOperationContext<T> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    fun unionAll(other: Subquery<T>): SqlSetOperationContext<T> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(kind: SqlSetOperationKind, other: Subquery<T>): SqlSetOperationContext<T> {
        return context.copy(
            kind = kind,
            left = SubqueryContext.SqlSetOperation(context),
            right = other.subqueryContext
        )
    }

    fun orderBy(vararg aliases: CharSequence): SqlSetOperationContext<T> {
        val items = aliases.map {
            if (it is SortItem) it else SortItem.Alias.Asc(it.toString())
        }
        return orderBy(items)
    }

    fun orderBy(vararg expressions: ColumnExpression<*, *>): SqlSetOperationContext<T> {
        val items = expressions.map {
            if (it is SortItem) it else SortItem.Property.Asc(it)
        }
        return orderBy(items)
    }

    private fun orderBy(items: List<SortItem>): SqlSetOperationContext<T> {
        return context.copy(orderBy = context.orderBy + items)
    }
}
