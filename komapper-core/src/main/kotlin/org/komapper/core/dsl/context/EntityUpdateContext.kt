package org.komapper.core.dsl.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.Table

internal data class EntityUpdateContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
) : Context<ENTITY> {

    override fun getTables(): List<Table> {
        return listOf(entityMetamodel)
    }
}
