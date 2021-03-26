package org.komapper.core.query

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.context.JoinContext
import org.komapper.core.query.context.JoinKind
import org.komapper.core.query.context.SelectContext
import org.komapper.core.query.data.SortItem
import org.komapper.core.query.scope.FilterScopeSupport
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.JoinScope
import org.komapper.core.query.scope.WhereDeclaration
import org.komapper.core.query.scope.WhereScope

internal class SelectQuerySupport<ENTITY>(
    private val context: SelectContext<ENTITY>
) {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ) {
        join(entityMetamodel, declaration, JoinKind.INNER)
    }

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ) {
        join(entityMetamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun <OTHER_ENTITY> join(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>,
        kind: JoinKind
    ) {
        val join = JoinContext(entityMetamodel, kind)
        val scope = JoinScope(join)
        declaration(scope)
        if (join.isNotEmpty()) {
            context.joins.add(join)
        }
    }

    fun where(declaration: WhereDeclaration) {
        val support = FilterScopeSupport(context.where)
        val scope = WhereScope(support)
        declaration(scope)
    }

    fun orderBy(vararg sortItems: ColumnInfo<*>) {
        for (item in sortItems) {
            when (item) {
                is SortItem -> context.orderBy.add(item)
                else -> context.orderBy.add(SortItem.Asc(item))
            }
        }
    }

    fun offset(value: Int) {
        context.offset = value
    }

    fun limit(value: Int) {
        context.limit = value
    }

    fun forUpdate() {
        context.forUpdate.option = ForUpdateOption.BASIC
    }
}
