package org.komapper.r2dbc.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.r2dbc.dsl.query.EntityDeleteQueryBuilder
import org.komapper.r2dbc.dsl.query.EntityDeleteQueryBuilderImpl
import org.komapper.r2dbc.dsl.query.EntityInsertQueryBuilder
import org.komapper.r2dbc.dsl.query.EntityInsertQueryBuilderImpl
import org.komapper.r2dbc.dsl.query.EntitySelectQuery
import org.komapper.r2dbc.dsl.query.EntitySelectQueryImpl
import org.komapper.r2dbc.dsl.query.EntityUpdateQueryBuilder
import org.komapper.r2dbc.dsl.query.EntityUpdateQueryBuilderImpl

object R2dbcEntityDsl {

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META
    ): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(EntitySelectContext(metamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): EntityInsertQueryBuilder<ENTITY, ID, META> {
        return EntityInsertQueryBuilderImpl(EntityInsertContext(metamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): EntityUpdateQueryBuilder<ENTITY> {
        return EntityUpdateQueryBuilderImpl(EntityUpdateContext(metamodel))
    }

    fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): EntityDeleteQueryBuilder<ENTITY> {
        return EntityDeleteQueryBuilderImpl(EntityDeleteContext(metamodel))
    }
}
