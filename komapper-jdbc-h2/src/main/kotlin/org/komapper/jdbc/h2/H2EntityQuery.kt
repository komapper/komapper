package org.komapper.jdbc.h2

import org.komapper.core.dsl.metamodel.EntityMetamodel

object H2EntityQuery {

    private object Messages {
        const val idPropertyRequired = "The entity metamodel must have one or more id properties."
    }

    fun <ENTITY : Any> merge(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entity: ENTITY
    ): EntityMergeQuery<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntityMergeQueryImpl(EntityMergeContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any> batchMerge(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchMergeQuery<ENTITY> {
        // TODO
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntityBatchMergeQueryImpl(EntityMergeContext(entityMetamodel), entities)
    }

    private fun <ENTITY : Any> hasIdProperty(entityMetamodel: EntityMetamodel<ENTITY>): Boolean {
        return entityMetamodel.idProperties().isNotEmpty()
    }
}
