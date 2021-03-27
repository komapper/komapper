package org.komapper.core.query

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.context.ForUpdate
import org.komapper.core.query.context.Join
import org.komapper.core.query.context.JoinContext
import org.komapper.core.query.context.JoinKind
import org.komapper.core.query.context.SelectContext
import org.komapper.core.query.data.SortItem
import org.komapper.core.query.scope.JoinDeclaration
import org.komapper.core.query.scope.JoinScope
import org.komapper.core.query.scope.WhereDeclaration
import org.komapper.core.query.scope.WhereScope

internal data class SelectQuerySupport<ENTITY, CONTEXT : SelectContext<ENTITY, CONTEXT>>(
    private val context: CONTEXT
) {

    fun <OTHER_ENTITY> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): CONTEXT {
        return join(entityMetamodel, declaration, JoinKind.INNER)
    }

    fun <OTHER_ENTITY> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>
    ): CONTEXT {
        return join(entityMetamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun <OTHER_ENTITY> join(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: JoinDeclaration<OTHER_ENTITY>,
        kind: JoinKind
    ): CONTEXT {
        val joinContext = JoinContext()
        val scope = JoinScope<OTHER_ENTITY>(joinContext)
        declaration(scope)
        if (joinContext.isNotEmpty()) {
            val join = Join(entityMetamodel, kind, joinContext.on.toList())
            return context.addJoin(join)
        }
        return context
    }

    fun where(declaration: WhereDeclaration): CONTEXT {
        val scope = WhereScope()
        declaration(scope)
        return context.addWhere(scope.criteria.toList())
    }

    fun orderBy(vararg sortItems: ColumnInfo<*>): CONTEXT {
        val items = sortItems.map {
            when (it) {
                is SortItem -> it
                else -> SortItem.Asc(it)
            }
        }
        return context.addOrderBy(items)
    }

    fun offset(value: Int): CONTEXT {
        return context.setOffset(value)
    }

    fun limit(value: Int): CONTEXT {
        return context.setLimit(value)
    }

    fun forUpdate(): CONTEXT {
        val forUpdate = ForUpdate(ForUpdateOption.BASIC)
        return context.setForUpdate(forUpdate)
    }
}
