package org.komapper.core.query

import org.komapper.core.metamodel.EntityMetamodel

object SubQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSelectSubQuery<ENTITY> {
        return SqlSelectQueryImpl(entityMetamodel)
    }
}
