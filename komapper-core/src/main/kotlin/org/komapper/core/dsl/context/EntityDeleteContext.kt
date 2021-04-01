package org.komapper.core.dsl.context

import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityDeleteContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
