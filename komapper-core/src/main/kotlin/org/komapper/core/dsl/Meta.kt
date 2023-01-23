package org.komapper.core.dsl

import org.komapper.core.EntityMetamodels
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel

/**
 * The entry point for entity metamodels.
 */
@ThreadSafe
object Meta {
    fun all(): List<EntityMetamodel<*, *, *>> {
        return EntityMetamodels.list(Meta)
    }
}
