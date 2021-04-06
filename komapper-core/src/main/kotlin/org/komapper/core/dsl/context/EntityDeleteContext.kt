package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntityDeleteContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>
) : Context<ENTITY> {

    override fun getEntityExpressions(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
