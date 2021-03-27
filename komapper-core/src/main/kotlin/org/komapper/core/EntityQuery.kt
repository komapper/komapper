package org.komapper.core

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.EntityDeleteQuery
import org.komapper.core.query.EntityInsertQuery
import org.komapper.core.query.EntitySelectQuery
import org.komapper.core.query.EntitySelectQueryImpl
import org.komapper.core.query.EntityUpdateQuery
import org.komapper.core.query.Query

object EntityQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(entityMetamodel)
    }

    fun <ENTITY> insert(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Query<ENTITY> {
        return EntityInsertQuery(entityMetamodel, entity)
    }

    fun <ENTITY> update(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Query<ENTITY> {
        return EntityUpdateQuery(entityMetamodel, entity)
    }

    fun <ENTITY> delete(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Query<Unit> {
        return EntityDeleteQuery(entityMetamodel, entity)
    }
}
