package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel

internal class EntityInsertContext<ENTITY>(val entityMetamodel: EntityMetamodel<ENTITY>) : Context<ENTITY> {

    override fun getEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
