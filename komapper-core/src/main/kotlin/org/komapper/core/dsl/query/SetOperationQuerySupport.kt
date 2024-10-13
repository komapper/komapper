package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SetOperationKind
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.SubqueryExpression

internal data class SetOperationQuerySupport<T : Any?>(
    private val context: SetOperationContext,
) {
    fun except(other: SubqueryExpression<T>): SetOperationContext {
        return setOperation(SetOperationKind.EXCEPT, other)
    }

    fun intersect(other: SubqueryExpression<T>): SetOperationContext {
        return setOperation(SetOperationKind.INTERSECT, other)
    }

    fun union(other: SubqueryExpression<T>): SetOperationContext {
        return setOperation(SetOperationKind.UNION, other)
    }

    fun unionAll(other: SubqueryExpression<T>): SetOperationContext {
        return setOperation(SetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(kind: SetOperationKind, other: SubqueryExpression<T>): SetOperationContext {
        return context.copy(
            kind = kind,
            left = context,
            right = other.context,
        )
    }

    fun orderBy(vararg aliases: CharSequence): SetOperationContext {
        val items = aliases.map {
            if (it is SortItem) it else SortItem.Alias.Asc(it.toString())
        }
        return orderBy(items)
    }

    fun orderBy(vararg expressions: SortExpression): SetOperationContext {
        val items = expressions.map(SortItem.Column::of)
        return orderBy(items)
    }

    private fun orderBy(items: List<SortItem>): SetOperationContext {
        return context.copy(orderBy = context.orderBy + items)
    }
}
