package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class SetOperationContext(
    val kind: SetOperationKind,
    val left: SubqueryContext,
    val right: SubqueryContext,
    val orderBy: List<SortItem> = listOf()
) : QueryContext, SubqueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return visitSubqueryContext(left) + visitSubqueryContext(right)
    }

    private fun visitSubqueryContext(subqueryContext: SubqueryContext): Set<EntityMetamodel<*, *, *>> {
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
