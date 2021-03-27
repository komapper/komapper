package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityDeleteContext<ENTITY>(val entityMetamodel: EntityMetamodel<ENTITY>) : Context<ENTITY> {

    override fun getReferencingEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
