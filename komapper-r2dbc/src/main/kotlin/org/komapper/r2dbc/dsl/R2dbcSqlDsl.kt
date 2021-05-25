package org.komapper.r2dbc.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.r2dbc.dsl.query.SqlDeleteQuery
import org.komapper.r2dbc.dsl.query.SqlDeleteQueryImpl
import org.komapper.r2dbc.dsl.query.SqlInsertQueryBuilder
import org.komapper.r2dbc.dsl.query.SqlInsertQueryBuilderImpl
import org.komapper.r2dbc.dsl.query.SqlInsertQueryImpl
import org.komapper.r2dbc.dsl.query.SqlSelectQuery
import org.komapper.r2dbc.dsl.query.SqlSelectQueryImpl
import org.komapper.r2dbc.dsl.query.SqlUpdateQueryBuilder
import org.komapper.r2dbc.dsl.query.SqlUpdateQueryBuilderImpl
import org.komapper.r2dbc.dsl.query.SqlUpdateQueryImpl

object R2dbcSqlDsl {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(metamodel: META): SqlSelectQuery<ENTITY> {
        return SqlSelectQueryImpl(SqlSelectContext(metamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(metamodel: META): SqlInsertQueryBuilder<ENTITY> {
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
