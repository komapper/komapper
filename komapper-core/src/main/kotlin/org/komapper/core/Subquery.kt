package org.komapper.core

import org.komapper.core.dsl.query.SelectSubquery
import org.komapper.core.dsl.query.SelectSubqueryImpl
import org.komapper.core.metamodel.EntityMetamodel

object Subquery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SelectSubquery<ENTITY> {
        return SelectSubqueryImpl(entityMetamodel)
    }
}
