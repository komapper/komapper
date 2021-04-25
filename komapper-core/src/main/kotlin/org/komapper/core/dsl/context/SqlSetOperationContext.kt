package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlSetOperationContext<T>(
    val kind: SqlSetOperationKind,
    val left: SubqueryContext<T>,
    val right: SubqueryContext<T>,
    val orderBy: List<SortItem> = listOf()
) : Context {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return visitSubqueryContext(left) + visitSubqueryContext(right)
    }

    private fun visitSubqueryContext(subqueryContext: SubqueryContext<*>): Set<EntityMetamodel<*, *, *>> {
        return when (subqueryContext) {
            is SubqueryContext.EntitySelect -> setOf(subqueryContext.context.target)
            is SubqueryContext.SqlSelect -> setOf(subqueryContext.context.target)
            is SubqueryContext.SqlSetOperation -> {
                visitSubqueryContext(subqueryContext.context.left) + visitSubqueryContext(subqueryContext.context.right)
            }
        }
    }
}

enum class SqlSetOperationKind {
    EXCEPT, INTERSECT, UNION, UNION_ALL
}
