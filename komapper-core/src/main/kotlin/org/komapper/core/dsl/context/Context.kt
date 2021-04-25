package org.komapper.core.dsl.context

import org.komapper.core.dsl.metamodel.EntityMetamodel

interface Context {
    fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>>
}
