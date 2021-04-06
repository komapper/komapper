package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntityInsertContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>
) : Context<ENTITY> {

    override fun getEntityExpressions(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
