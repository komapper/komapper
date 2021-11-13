package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel

data class EntityDeleteContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }

    fun asSqlDeleteContext(): SqlDeleteContext<ENTITY, ID, META> {
        return SqlDeleteContext(target)
    }
}
