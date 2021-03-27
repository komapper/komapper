package org.komapper.core

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.SqlSelectQuery
import org.komapper.core.query.SqlSelectQueryImpl

object SqlQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(entityMetamodel)
    }
}
