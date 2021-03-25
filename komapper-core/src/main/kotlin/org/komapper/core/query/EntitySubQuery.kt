package org.komapper.core.query

import org.komapper.core.metamodel.EntityMetamodel

object EntitySubQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): EntitySelectSubQuery<ENTITY> {
        return EntitySelectQueryImpl(entityMetamodel)
    }
}
