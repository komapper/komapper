package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel

internal interface Context<ENTITY> {
    fun getEntityMetamodels(): List<EntityMetamodel<*>>
}
