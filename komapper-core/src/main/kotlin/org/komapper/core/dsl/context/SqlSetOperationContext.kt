package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.EntityExpression

internal data class SqlSetOperationContext<T>(
    val component: SqlSetOperationComponent<T>,
    val orderBy: List<SortItem> = listOf()
) : Context {
    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return visitComponent(component)
    }

    private fun visitComponent(component: SqlSetOperationComponent<*>): Set<EntityExpression<*>> {
        return when (component) {
            is SqlSetOperationComponent.Leaf -> setOf(component.context.entityMetamodel)
            is SqlSetOperationComponent.Composite -> {
                visitComponent(component.left) + visitComponent(component.right)
            }
        }
    }
}

sealed class SqlSetOperationComponent<T> {
    internal class Leaf<T>(val context: SqlSelectContext<*>) : SqlSetOperationComponent<T>()
    internal class Composite<T>(
        val kind: SqlSetOperationKind,
        val left: SqlSetOperationComponent<T>,
        val right: SqlSetOperationComponent<T>
    ) :
        SqlSetOperationComponent<T>()
}

enum class SqlSetOperationKind {
    EXCEPT, INTERSECT, UNION, UNION_ALL
}
