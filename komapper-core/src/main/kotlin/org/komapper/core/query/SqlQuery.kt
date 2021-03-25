package org.komapper.core.query

import org.komapper.core.metamodel.EntityMetamodel

object SqlQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(entityMetamodel)
    }
}
