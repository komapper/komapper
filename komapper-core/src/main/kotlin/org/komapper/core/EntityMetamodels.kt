package org.komapper.core

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.spi.EntityMetamodelFactory
import java.util.ServiceLoader

/**
 * The provider of [EntityMetamodel].
 *
 * This class requires the following configuration in build.gradle.kts:
 *
 * ```kotlin
 * ksp {
 *   arg("komapper.enableEntityMetamodelListing", "true")
 * }
 * ```
 */
object EntityMetamodels {

    /**
     * Returns a list of entity metamodel unit and entity metamodel pairs.
     *
     * @return a list
     */
    fun all(): List<Pair<Any, EntityMetamodel<*, *, *>>> {
        val loader = ServiceLoader.load(EntityMetamodelFactory::class.java)
        return loader.map { it.create() }.flatten()
    }

    /**
     * Returns a list of entity metamodels belonging to a specific unit.
     *
     * @param unit the entity metamodel unit such as [org.komapper.core.dsl.Meta]
     * @return a list of entity metamodels
     */
    fun list(unit: Any): List<EntityMetamodel<*, *, *>> {
        return all().filter { it.first == unit }.map { it.second }
    }
}
