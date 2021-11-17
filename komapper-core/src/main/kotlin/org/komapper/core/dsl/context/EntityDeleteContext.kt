package org.komapper.core.dsl.context

import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.where

data class EntityDeleteContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }

    override fun getWhereDeclarations(): List<WhereDeclaration> {
        return target.where
    }

    fun asSqlDeleteContext(): SqlDeleteContext<ENTITY, ID, META> {
        return SqlDeleteContext(target)
    }
}
