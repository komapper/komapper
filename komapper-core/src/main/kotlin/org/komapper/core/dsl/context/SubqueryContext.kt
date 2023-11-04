package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AliasExpression
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.InlineViewMetamodel

@ThreadSafe
sealed interface SubqueryContext

internal val SubqueryContext.inlineViewMetamodel: InlineViewMetamodel
    get() = InlineViewMetamodel(this)

internal val SubqueryContext.columns: List<ColumnExpression<*, *>>
    get() = when (this) {
        is SelectContext<*, *, *> -> getProjection().expressions()
        is SetOperationContext -> left.columns
    }

internal val SubqueryContext.columnAndAliasPairs: List<Pair<ColumnExpression<*, *>, String>>
    get() = columns.mapIndexed { index, column ->
        val alias = when (column) {
            is AliasExpression<*, *> -> column.alias
            else -> buildAlias(column, index)
        }
        column to alias
    }

internal fun SubqueryContext.makeAlias(column: ColumnExpression<*, *>): String {
    val index = columns.indexOf(column)
    return if (index > -1) {
        buildAlias(column, index)
    } else {
        error("The column \"${column.columnName}\" is not found in the select list of the subquery.")
    }
}

private fun buildAlias(column: ColumnExpression<*, *>, index: Int): String {
    val alias = "c${index}_${column.columnName}_"
    return if (alias.length > 50) alias.substring(0, 50) else alias
}
