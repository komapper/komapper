package org.komapper.jdbc.dsl

import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.dsl.query.SqlDeleteQuery
import org.komapper.jdbc.dsl.query.SqlDeleteQueryImpl
import org.komapper.jdbc.dsl.query.SqlInsertQueryBuilder
import org.komapper.jdbc.dsl.query.SqlInsertQueryBuilderImpl
import org.komapper.jdbc.dsl.query.SqlInsertQueryImpl
import org.komapper.jdbc.dsl.query.SqlSelectQuery
import org.komapper.jdbc.dsl.query.SqlSelectQueryImpl
import org.komapper.jdbc.dsl.query.SqlUpdateQueryBuilder
import org.komapper.jdbc.dsl.query.SqlUpdateQueryBuilderImpl
import org.komapper.jdbc.dsl.query.SqlUpdateQueryImpl

object SqlDsl : Dsl {

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
