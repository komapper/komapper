package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.JoinKind
import org.komapper.core.dsl.expression.OnDeclaration
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.ForUpdateOptions

internal class SelectQuerySupport<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, CONTEXT : SelectContext<ENTITY, ID, META, CONTEXT>>(
    private val context: CONTEXT
) {

    fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> innerJoin(
        metamodel: META2,
        declaration: OnDeclaration
    ): CONTEXT {
        return join(metamodel, declaration, JoinKind.INNER)
    }

    fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> leftJoin(
        metamodel: META2,
        declaration: OnDeclaration
    ): CONTEXT {
        return join(metamodel, declaration, JoinKind.LEFT_OUTER)
    }

    private fun <ENTITY2 : Any, ID2, META2 : EntityMetamodel<ENTITY2, ID2, META2>> join(
        metamodel: META2,
        declaration: OnDeclaration,
        kind: JoinKind
    ): CONTEXT {
        val join = Join(metamodel, kind, declaration)
        return context.addJoin(join)
    }

    fun where(declaration: WhereDeclaration): CONTEXT {
        return context.addWhere(declaration)
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
