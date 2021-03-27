package org.komapper.core.query

import org.komapper.core.metamodel.EntityMetamodel

object EntityQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(entityMetamodel)
    }

    fun <ENTITY> insert(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityInsertQuery<ENTITY> {
        return EntityInsertQueryImpl(entityMetamodel, entity)
    }

    fun <ENTITY> update(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityUpdateQuery<ENTITY> {
        return EntityUpdateQueryImpl(entityMetamodel, entity)
    }

    fun <ENTITY> delete(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): EntityDeleteQuery<ENTITY> {
        return EntityDeleteQueryImpl(entityMetamodel, entity)
    }
}
