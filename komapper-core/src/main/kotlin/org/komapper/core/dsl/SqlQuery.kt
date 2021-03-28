package org.komapper.core.dsl

import org.komapper.core.dsl.query.SqlSelectQuery
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.metamodel.EntityMetamodel

object SqlQuery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(entityMetamodel)
    }
}
