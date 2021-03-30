package org.komapper.core.dsl.context

import org.komapper.core.config.EntityDeleteOptions
import org.komapper.core.config.OptionsImpl
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityDeleteContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val options: EntityDeleteOptions = OptionsImpl()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
