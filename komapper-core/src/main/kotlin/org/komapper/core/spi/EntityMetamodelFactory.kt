package org.komapper.core.spi

import org.komapper.core.dsl.metamodel.EntityMetamodel

interface EntityMetamodelFactory {
    fun create(): List<Pair<Any, EntityMetamodel<*, *, *>>>
}
