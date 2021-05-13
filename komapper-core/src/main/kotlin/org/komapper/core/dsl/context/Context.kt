package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
interface Context {
    fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>>
}
