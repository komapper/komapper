package org.komapper.core.dsl.context

import org.komapper.core.metamodel.EntityMetamodel

internal interface Context<ENTITY> {
    fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>>
}
