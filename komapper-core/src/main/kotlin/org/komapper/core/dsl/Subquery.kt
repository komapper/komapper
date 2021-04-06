package org.komapper.core.dsl

import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.SqlSubquery
import org.komapper.core.dsl.query.SqlSubqueryImpl

object Subquery : Dsl {

    fun <ENTITY> from(entityMetamodel: EntityMetamodel<ENTITY>): SqlSubquery<ENTITY> {
        return SqlSubqueryImpl(SqlSelectContext(entityMetamodel))
    }
}
