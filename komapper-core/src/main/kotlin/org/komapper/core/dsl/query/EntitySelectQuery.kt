package org.komapper.core.dsl.query

import org.komapper.core.dsl.metamodel.EntityMetamodel

/**
 * Represents the query to retrieve entities.
 * This query returns an entity or entities.
 * @param ENTITY the entity type
 */
interface EntitySelectQuery<ENTITY : Any> : SelectQuery<ENTITY, EntitySelectQuery<ENTITY>> {
    /**
     * Includes the columns corresponding to the metamodels in the projection of the query.
     * @param metamodels the entity metamodels
     * @return the query
     */
    fun include(vararg metamodels: EntityMetamodel<*, *, *>): EntityStoreQuery

    /**
     * Includes the columns corresponding to all the metamodels in the projection of the query.
     * @return the query
     */
    fun includeAll(): EntityStoreQuery
}
