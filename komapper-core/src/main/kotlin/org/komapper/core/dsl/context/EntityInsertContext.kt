package org.komapper.core.dsl.context

import org.komapper.core.config.EntityInsertOptions
import org.komapper.core.config.OptionsImpl
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntityInsertContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val options: EntityInsertOptions = OptionsImpl()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }
}
