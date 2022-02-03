package org.komapper.core.dsl

import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.SchemaCreateQuery
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropQuery
import org.komapper.core.dsl.query.SchemaDropQueryImpl

/**
 * The entry point for constructing schema related queries.
 */
object SchemaDsl : Dsl {

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(SchemaContext(metamodels))
    }

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return create(metamodels.toList())
    }

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(SchemaContext(metamodels))
    }

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return drop(metamodels.toList())
    }
}
