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

/**
 * The entry point for constructing SELECT, INSERT, UPDATE, and DELETE queries.
 */
object QueryDsl : Dsl {

    /**
     * Creates a SELECT query.
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): EntitySelectQuery<ENTITY> {
        return EntitySelectQueryImpl(SelectContext(metamodel))
    }

    /**
     * Creates a INSERT query builder.
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): EntityInsertQueryBuilder<ENTITY, ID, META> {
        return EntityInsertQueryBuilderImpl(EntityInsertContext(metamodel))
    }

    /**
     * Creates a UPDATE query builder.
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): EntityUpdateQueryBuilder<ENTITY, ID, META> {
        return EntityUpdateQueryBuilderImpl(EntityUpdateContext(metamodel))
    }

    /**
     * Creates a DELETE query builder.
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): EntityDeleteQueryBuilder<ENTITY> {
        return EntityDeleteQueryBuilderImpl(EntityDeleteContext(metamodel))
    }
}
