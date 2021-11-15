package org.komapper.core.dsl.context

import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class SqlDeleteContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val where: List<WhereDeclaration> = listOf()
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }
}
