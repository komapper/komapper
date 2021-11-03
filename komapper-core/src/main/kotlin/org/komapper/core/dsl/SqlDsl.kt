package org.komapper.core.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityInsertQueryBuilder
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntityUpdateQueryBuilder
import org.komapper.core.dsl.query.SqlDeleteQuery
import org.komapper.core.dsl.query.SqlDeleteQueryImpl

object SqlDsl : Dsl {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(metamodel: META): EntitySelectQuery<ENTITY> {
        return EntityDsl.from(metamodel)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(metamodel: META): EntityInsertQueryBuilder<ENTITY, ID, META> {
        return EntityDsl.insert(metamodel)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> update(metamodel: META): EntityUpdateQueryBuilder<ENTITY> {
        return EntityDsl.update(metamodel)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> delete(metamodel: META): SqlDeleteQuery {
        return SqlDeleteQueryImpl(SqlDeleteContext(metamodel))
    }
}
