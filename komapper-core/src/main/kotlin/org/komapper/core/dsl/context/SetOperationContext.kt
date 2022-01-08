package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.options.SelectOptions

@ThreadSafe
data class SetOperationContext(
    val kind: SetOperationKind,
    val left: SubqueryContext,
    val right: SubqueryContext,
    val orderBy: List<SortItem> = listOf(),
    val options: SelectOptions = SelectOptions.DEFAULT
) : TablesProvider, SubqueryContext {

    override fun getTables(): Set<TableExpression<*>> {
        return visitSubqueryContext(left) + visitSubqueryContext(right)
    }

    private fun visitSubqueryContext(subqueryContext: SubqueryContext): Set<TableExpression<*>> {
        return when (subqueryContext) {
            is SelectContext<*, *, *> -> setOf(subqueryContext.target)
            is SetOperationContext -> {
                visitSubqueryContext(subqueryContext.left) + visitSubqueryContext(subqueryContext.right)
            }
        }
    }
}

enum class SetOperationKind {
    EXCEPT, INTERSECT, UNION, UNION_ALL
}
