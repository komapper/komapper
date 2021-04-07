package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.expr.NamedSortItem
import org.komapper.core.dsl.expr.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.ForUpdateOption
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope

internal data class SelectQuerySupport<ENTITY : Any, CONTEXT : SelectContext<ENTITY, CONTEXT>>(
    private val context: CONTEXT
) {

    fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: OnDeclaration<OTHER_ENTITY>
    ): CONTEXT {
        return join(entityMetamodel, declaration, JoinKind.INNER)
    }

    fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: OnDeclaration<OTHER_ENTITY>
    ): CONTEXT {
        return join(entityMetamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun <OTHER_ENTITY : Any> join(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        declaration: OnDeclaration<OTHER_ENTITY>,
        kind: JoinKind
    ): CONTEXT {
        val scope = OnScope<OTHER_ENTITY>()
        declaration(scope)
        if (scope.isNotEmpty()) {
            val join = Join(entityMetamodel, kind, scope.toList())
            return context.addJoin(join)
        }
        return context
    }

    fun where(declaration: WhereDeclaration): CONTEXT {
        val scope = WhereScope()
        declaration(scope)
        return context.addWhere(scope.toList())
    }

    fun orderBy(vararg properties: PropertyExpression<*>): CONTEXT {
        val items = properties.map {
            when (it) {
                is NamedSortItem -> it
                else -> NamedSortItem.Asc(it)
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
