package org.komapper.core.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityBatchDeleteQuery
import org.komapper.core.dsl.query.EntityBatchDeleteQueryImpl
import org.komapper.core.dsl.query.EntityBatchInsertQuery
import org.komapper.core.dsl.query.EntityBatchInsertQueryImpl
import org.komapper.core.dsl.query.EntityBatchUpdateQuery
import org.komapper.core.dsl.query.EntityBatchUpdateQueryImpl
import org.komapper.core.dsl.query.EntityDeleteQuery
import org.komapper.core.dsl.query.EntityDeleteQueryImpl
import org.komapper.core.dsl.query.EntityInsertQuery
import org.komapper.core.dsl.query.EntityInsertQueryImpl
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.EntityUpdateQueryImpl
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.scope.WhereDeclaration

object EntityQuery : Dsl {

    private object Messages {
        const val idPropertyRequired = "The entity metamodel must have one or more id properties."
        const val idValueRequired = "The entity must have one or more id value."
        fun idValueRequired(index: Int) = "The entity(index=$index) must have one or more id value."
    }

    fun <ENTITY : Any> first(
        entityMetamodel: EntityMetamodel<ENTITY>,
        declaration: WhereDeclaration
    ): Query<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
            .where(declaration)
            .limit(1)
            .first()
    }

    fun <ENTITY : Any> firstOrNull(
        entityMetamodel: EntityMetamodel<ENTITY>,
        declaration: WhereDeclaration
    ): Query<ENTITY?> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
            .where(declaration)
            .limit(1)
            .firstOrNull()
    }

    fun <ENTITY : Any> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQuery<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
    }

    fun <ENTITY : Any> insert(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityInsertQuery<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntityInsertQueryImpl(EntityInsertContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any> update(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityUpdateQuery<ENTITY> {
        require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired }
        return EntityUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any> delete(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityDeleteQuery<ENTITY> {
        require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired }
        return EntityDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entity)
    }

    fun <ENTITY : Any> insertBatch(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchInsertQuery<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntityBatchInsertQueryImpl(EntityInsertContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any> updateBatch(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchUpdateQuery<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        for ((i, entity) in entities.withIndex()) {
            require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired(i) }
        }
        return EntityBatchUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any> deleteBatch(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchDeleteQuery<ENTITY> {
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        for ((i, entity) in entities.withIndex()) {
            require(hasIdValue(entityMetamodel, entity)) { Messages.idValueRequired(i) }
        }
        return EntityBatchDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entities)
    }

    private fun <ENTITY : Any> hasIdProperty(entityMetamodel: EntityMetamodel<ENTITY>): Boolean {
        return entityMetamodel.idProperties().isNotEmpty()
    }

    private fun <ENTITY : Any> hasIdValue(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Boolean {
        return entityMetamodel.idProperties().map { it.getter(entity) }.all { it != null }
    }
}
