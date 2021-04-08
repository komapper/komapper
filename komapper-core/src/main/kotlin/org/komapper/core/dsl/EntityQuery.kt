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
import org.komapper.core.dsl.query.EntityFindQuery
import org.komapper.core.dsl.query.EntityFindQueryImpl
import org.komapper.core.dsl.query.EntityInsertQuery
import org.komapper.core.dsl.query.EntityInsertQueryImpl
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.EntityUpdateQueryImpl
import org.komapper.core.dsl.query.ListQuery
import org.komapper.core.dsl.query.Query

object EntityQuery : Dsl {

    private object Messages {
        const val mustNotBeEmpty = "The entity list must not be empty."
        const val idPropertyRequired = "The entity metamodel must have one or more id properties."
        const val idValueRequired = "The entity must have one or more id value."
    }

    fun <ENTITY : Any> first(entityMetamodel: EntityMetamodel<ENTITY>): EntityFindQuery<ENTITY, ENTITY> {
        return createFindQuery(entityMetamodel) { it.first() }
    }

    fun <ENTITY : Any> firstOrNull(entityMetamodel: EntityMetamodel<ENTITY>): EntityFindQuery<ENTITY, ENTITY?> {
        return createFindQuery(entityMetamodel) { it.firstOrNull() }
    }

    private fun <ENTITY : Any, R> createFindQuery(
        entityMetamodel: EntityMetamodel<ENTITY>,
        transformer: (ListQuery<ENTITY>) -> Query<R>
    ): EntityFindQuery<ENTITY, R> {
        val selectQuery = EntitySelectQueryImpl(EntitySelectContext(entityMetamodel)).limit(1)
        return EntityFindQueryImpl(selectQuery, transformer)
    }

    fun <ENTITY : Any> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQuery<ENTITY> {
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

    fun <ENTITY : Any> batchInsert(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchInsertQuery<ENTITY> {
        require(entities.isNotEmpty()) { Messages.mustNotBeEmpty }
        require(hasIdProperty(entityMetamodel)) { Messages.idPropertyRequired }
        return EntityBatchInsertQueryImpl(EntityInsertContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any> batchUpdate(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchUpdateQuery<ENTITY> {
        require(entities.isNotEmpty()) { Messages.mustNotBeEmpty }
        require(entities.all { hasIdValue(entityMetamodel, it) }) { Messages.idValueRequired }
        return EntityBatchUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entities)
    }

    fun <ENTITY : Any> batchDelete(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchDeleteQuery<ENTITY> {
        require(entities.isNotEmpty()) { Messages.mustNotBeEmpty }
        require(entities.all { hasIdValue(entityMetamodel, it) }) { Messages.idValueRequired }
        return EntityBatchDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entities)
    }

    private fun <ENTITY : Any> hasIdProperty(entityMetamodel: EntityMetamodel<ENTITY>): Boolean {
        return entityMetamodel.idProperties().isNotEmpty()
    }

    private fun <ENTITY : Any> hasIdValue(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Boolean {
        return hasIdProperty(entityMetamodel) &&
            entityMetamodel.idProperties().map { it.getter(entity) }.all { it != null }
    }
}
