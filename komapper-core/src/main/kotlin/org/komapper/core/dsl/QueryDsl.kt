package org.komapper.core.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityDeleteQueryBuilder
import org.komapper.core.dsl.query.EntityDeleteQueryBuilderImpl
import org.komapper.core.dsl.query.EntityInsertQueryBuilder
import org.komapper.core.dsl.query.EntityInsertQueryBuilderImpl
import org.komapper.core.dsl.query.EntitySelectQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntityUpdateQueryBuilder
import org.komapper.core.dsl.query.EntityUpdateQueryBuilderImpl

object QueryDsl : Dsl {

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(SelectContext(metamodel))
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): EntityInsertQueryBuilder<ENTITY, ID, META> {
        return EntityInsertQueryBuilderImpl(EntityInsertContext(metamodel))
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): EntityUpdateQueryBuilder<ENTITY, ID, META> {
        return EntityUpdateQueryBuilderImpl(EntityUpdateContext(metamodel))
    }

    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): EntityDeleteQueryBuilder<ENTITY> {
        return EntityDeleteQueryBuilderImpl(EntityDeleteContext(metamodel))
    }
}
