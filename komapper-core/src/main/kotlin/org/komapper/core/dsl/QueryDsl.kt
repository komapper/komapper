package org.komapper.core.dsl

import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.DeleteQueryBuilder
import org.komapper.core.dsl.query.DeleteQueryBuilderImpl
import org.komapper.core.dsl.query.InsertQueryBuilder
import org.komapper.core.dsl.query.InsertQueryBuilderImpl
import org.komapper.core.dsl.query.SelectQueryBuilder
import org.komapper.core.dsl.query.SelectQueryBuilderImpl
import org.komapper.core.dsl.query.UpdateQueryBuilder
import org.komapper.core.dsl.query.UpdateQueryBuilderImpl

/**
 * The entry point for constructing SELECT, INSERT, UPDATE, and DELETE queries.
 */
object QueryDsl : Dsl {

    /**
     * Creates a SELECT query builder.
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel))
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
    ): InsertQueryBuilder<ENTITY, ID, META> {
        return InsertQueryBuilderImpl(EntityInsertContext(metamodel))
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
    ): UpdateQueryBuilder<ENTITY, ID, META> {
        return UpdateQueryBuilderImpl(EntityUpdateContext(metamodel))
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
    ): DeleteQueryBuilder<ENTITY> {
        return DeleteQueryBuilderImpl(EntityDeleteContext(metamodel))
    }
}
