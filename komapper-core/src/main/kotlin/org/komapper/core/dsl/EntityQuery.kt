package org.komapper.core.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.query.EntityBatchDeleteQuery
import org.komapper.core.dsl.query.EntityBatchDeleteQueryImpl
import org.komapper.core.dsl.query.EntityBatchInsertQuery
import org.komapper.core.dsl.query.EntityBatchInsertQueryImpl
import org.komapper.core.dsl.query.EntityBatchUpdateQuery
import org.komapper.core.dsl.query.EntityBatchUpdateQueryImpl
import org.komapper.core.dsl.query.EntityDeleteQuery
import org.komapper.core.dsl.query.EntityDeleteQueryImpl
import org.komapper.core.dsl.query.EntityFindQuery
import org.komapper.core.dsl.query.EntityFirstOrNullQuery
import org.komapper.core.dsl.query.EntityFirstQuery
import org.komapper.core.dsl.query.EntityInsertQuery
import org.komapper.core.dsl.query.EntityInsertQueryImpl
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.EntityUpdateQueryImpl
import org.komapper.core.metamodel.EntityMetamodel

object EntityQuery : Dsl {

    fun <ENTITY> first(entityMetamodel: EntityMetamodel<ENTITY>): EntityFindQuery<ENTITY> {
        val selectQuery = EntitySelectQueryImpl(EntitySelectContext(entityMetamodel)).limit(1)
        return EntityFirstQuery(selectQuery)
    }

    fun <ENTITY> firstOrNull(entityMetamodel: EntityMetamodel<ENTITY>): EntityFindQuery<ENTITY?> {
        val selectQuery = EntitySelectQueryImpl(EntitySelectContext(entityMetamodel)).limit(1)
        return EntityFirstOrNullQuery(selectQuery)
    }

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
    }

    fun <ENTITY> insert(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityInsertQuery<ENTITY> {
        return EntityInsertQueryImpl(EntityInsertContext(entityMetamodel), entity)
    }

    fun <ENTITY> update(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityUpdateQuery<ENTITY> {
        return EntityUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entity)
    }

    fun <ENTITY> delete(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityDeleteQuery<ENTITY> {
        return EntityDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entity)
    }

    fun <ENTITY> batchInsert(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchInsertQuery<ENTITY> {
        require(entities.isNotEmpty()) { "The 'entities' list must not be empty." }
        return EntityBatchInsertQueryImpl(EntityInsertContext(entityMetamodel), entities)
    }

    fun <ENTITY> batchUpdate(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchUpdateQuery<ENTITY> {
        require(entities.isNotEmpty()) { "The 'entities' list must not be empty." }
        return EntityBatchUpdateQueryImpl(EntityUpdateContext(entityMetamodel), entities)
    }

    fun <ENTITY> batchDelete(
        entityMetamodel: EntityMetamodel<ENTITY>,
        entities: List<ENTITY>
    ): EntityBatchDeleteQuery<ENTITY> {
        require(entities.isNotEmpty()) { "The 'entities' list must not be empty." }
        return EntityBatchDeleteQueryImpl(EntityDeleteContext(entityMetamodel), entities)
    }
}
