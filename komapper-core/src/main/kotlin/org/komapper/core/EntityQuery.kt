package org.komapper.core

import org.komapper.core.dsl.query.EntityDeleteQuery
import org.komapper.core.dsl.query.EntityInsertQuery
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.metamodel.EntityMetamodel

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
