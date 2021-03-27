package org.komapper.core

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.SelectSubquery
import org.komapper.core.query.SelectSubqueryImpl

object Subquery {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SelectSubquery<ENTITY> {
        return SelectSubqueryImpl(entityMetamodel)
    }
}
