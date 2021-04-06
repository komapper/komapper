package org.komapper.core.dsl.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.Table

internal interface Context<ENTITY> {
    val entityMetamodel: EntityMetamodel<ENTITY>
    fun getTables(): List<Table>
}
