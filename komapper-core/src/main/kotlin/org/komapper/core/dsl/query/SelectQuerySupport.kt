package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.declaration.OnDeclaration
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.ForUpdateOptions
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.WhereScope

internal class SelectQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, CONTEXT : SelectContext<ENTITY, ID, META, CONTEXT>>(
    private val context: CONTEXT
) {

    fun innerJoin(
        metamodel: EntityMetamodel<*, *, *>,
        declaration: OnDeclaration
    ): CONTEXT {
        return join(metamodel, declaration, JoinKind.INNER)
    }

    fun leftJoin(
        metamodel: EntityMetamodel<*, *, *>,
        declaration: OnDeclaration
    ): CONTEXT {
        return join(metamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun join(
        metamodel: EntityMetamodel<*, *, *>,
        declaration: OnDeclaration,
        kind: JoinKind
    ): CONTEXT {
        val scope = OnScope().apply(declaration)
        if (scope.isNotEmpty()) {
            val join = Join(metamodel, kind, scope.toList())
            return context.addJoin(join)
        }
        return context
    }

    fun where(declaration: WhereDeclaration): CONTEXT {
        val scope = WhereScope().apply(declaration)
        return context.addWhere(scope)
    }

    fun orderBy(vararg expressions: SortExpression): CONTEXT {
        val items = expressions.map(SortItem.Column::of)
        return context.addOrderBy(items)
    }

    fun offset(offset: Int): CONTEXT {
        return context.setOffset(offset)
    }

    fun limit(limit: Int): CONTEXT {
        return context.setLimit(limit)
    }

    fun forUpdate(): CONTEXT {
        val forUpdate = ForUpdate(ForUpdateOptions.BASIC)
        return context.setForUpdate(forUpdate)
    }
}
