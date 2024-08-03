package org.komapper.core.dsl

import org.komapper.core.EntityMetamodels
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel

/**
 * The entry point for entity metamodels.
 */
@ThreadSafe
object Meta {

    /**
     * Retrieves a list of all entity metamodels.
     *
     * This method requires the following configuration in build.gradle.kts:
     *
     * ```kotlin
     * ksp {
     *   arg("komapper.enableEntityMetamodelListing", "true")
     * }
     * ```
     *
     * @return a list of entity metamodels
     */
    fun all(): List<EntityMetamodel<*, *, *>> {
        return EntityMetamodels.list(Meta)
    }
}
