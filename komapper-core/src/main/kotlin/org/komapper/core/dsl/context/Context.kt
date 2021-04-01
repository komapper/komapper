package org.komapper.core.dsl.context

import org.komapper.core.metamodel.EntityMetamodel

internal interface Context<ENTITY> {
    val entityMetamodel: EntityMetamodel<ENTITY>
    fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>>
}
