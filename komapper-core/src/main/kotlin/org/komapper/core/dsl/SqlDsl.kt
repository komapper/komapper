package org.komapper.core.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.SqlDeleteQuery
import org.komapper.core.dsl.query.SqlDeleteQueryImpl
import org.komapper.core.dsl.query.SqlInsertQueryBuilder
import org.komapper.core.dsl.query.SqlInsertQueryBuilderImpl
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlUpdateQueryBuilder
import org.komapper.core.dsl.query.SqlUpdateQueryBuilderImpl
import org.komapper.core.dsl.query.SqlUpdateQueryImpl

object SqlDsl : Dsl {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(metamodel: META): EntitySelectQuery<ENTITY> {
        return EntityDsl.from(metamodel)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(metamodel: META): SqlInsertQueryBuilder<ENTITY, ID> {
        val query = SqlInsertQueryImpl(SqlInsertContext(metamodel))
        return SqlInsertQueryBuilderImpl(query)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> update(metamodel: META): SqlUpdateQueryBuilder<ENTITY> {
        val query = SqlUpdateQueryImpl(SqlUpdateContext(metamodel))
        return SqlUpdateQueryBuilderImpl(query)
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> delete(metamodel: META): SqlDeleteQuery {
        return SqlDeleteQueryImpl(SqlDeleteContext(metamodel))
    }
}
