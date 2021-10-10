package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class SqlSetOperationContext(
    val kind: SqlSetOperationKind,
    val left: SubqueryContext,
    val right: SubqueryContext,
    val orderBy: List<SortItem> = listOf()
) : Context, SubqueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return visitSubqueryContext(left) + visitSubqueryContext(right)
    }

    private fun visitSubqueryContext(subqueryContext: SubqueryContext): Set<EntityMetamodel<*, *, *>> {
        return when (subqueryContext) {
            is SelectContext<*, *, *, *> -> setOf(subqueryContext.target)
            is SqlSetOperationContext -> {
                visitSubqueryContext(subqueryContext.left) + visitSubqueryContext(subqueryContext.right)
            }
        }
    }
}

enum class SqlSetOperationKind {
    EXCEPT, INTERSECT, UNION, UNION_ALL
}
