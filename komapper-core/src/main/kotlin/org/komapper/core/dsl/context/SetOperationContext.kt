package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.options.SelectOptions

@ThreadSafe
data class SetOperationContext(
    val kind: SetOperationKind,
    val left: SubqueryContext,
    val right: SubqueryContext,
    val orderBy: List<SortItem> = listOf(),
    override val options: SelectOptions,
) : TablesProvider, SubqueryContext {
    fun getProjection(): Projection {
        fun visitSubqueryContext(subqueryContext: SubqueryContext): Projection {
            return when (subqueryContext) {
                is SelectContext<*, *, *> -> subqueryContext.getProjection()
                is SetOperationContext -> visitSubqueryContext(subqueryContext.left)
            }
        }
        return visitSubqueryContext(left)
    }

    override fun getTables(): Set<TableExpression<*>> {
        fun visitSubqueryContext(subqueryContext: SubqueryContext): Set<TableExpression<*>> {
            return when (subqueryContext) {
                is SelectContext<*, *, *> -> setOf(subqueryContext.target)
                is SetOperationContext -> {
                    visitSubqueryContext(subqueryContext.left) + visitSubqueryContext(subqueryContext.right)
                }
            }
        }
        return visitSubqueryContext(left) + visitSubqueryContext(right)
    }
}

enum class SetOperationKind {
    EXCEPT,
    INTERSECT,
    UNION,
    UNION_ALL
}
