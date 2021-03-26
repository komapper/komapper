package org.komapper.core.query

import org.komapper.core.metamodel.EntityMetamodel

object EntityQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQueryable<ENTITY> {
        return EntitySelectQueryableImpl(entityMetamodel)
    }

    fun <ENTITY> insert(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityInsertQueryable<ENTITY> {
        return EntityInsertQueryableImpl(entityMetamodel, entity)
    }

    fun <ENTITY> update(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityUpdateQueryable<ENTITY> {
        return EntityUpdateQueryableImpl(entityMetamodel, entity)
    }

    fun <ENTITY> delete(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityDeleteQueryable<ENTITY> {
        return EntityDeleteQueryableImpl(entityMetamodel, entity)
    }
}
