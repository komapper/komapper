package org.komapper.core.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.query.EntityDeleteQuery
import org.komapper.core.dsl.query.EntityInsertQuery
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQuery
import org.komapper.core.dsl.query.Query
import org.komapper.core.metamodel.EntityMetamodel

object EntityQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(entityMetamodel))
    }

    fun <ENTITY> insert(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Query<ENTITY> {
        return EntityInsertQuery(EntityInsertContext(entityMetamodel), entity)
    }

    fun <ENTITY> update(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Query<ENTITY> {
        return EntityUpdateQuery(EntityUpdateContext(entityMetamodel), entity)
    }

    fun <ENTITY> delete(entityMetamodel: EntityMetamodel<ENTITY>, entity: ENTITY): Query<Unit> {
        return EntityDeleteQuery(EntityDeleteContext(entityMetamodel), entity)
    }
}
