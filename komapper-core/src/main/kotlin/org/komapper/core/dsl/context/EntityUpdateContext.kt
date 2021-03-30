package org.komapper.core.dsl.context

import org.komapper.core.config.EntityUpdateOptions
import org.komapper.core.config.OptionsImpl
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityUpdateContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val options: EntityUpdateOptions = OptionsImpl()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
