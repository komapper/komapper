package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where

data class RelationDeleteContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val where: List<WhereDeclaration> = listOf()
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }

    override fun getWhereDeclarations(): List<WhereDeclaration> {
        return target.where + where
    }
}
