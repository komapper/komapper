package org.komapper.r2dbc.dsl

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.r2dbc.dsl.query.SchemaCreateQuery
import org.komapper.r2dbc.dsl.query.SchemaCreateQueryImpl
import org.komapper.r2dbc.dsl.query.SchemaDropAllQuery
import org.komapper.r2dbc.dsl.query.SchemaDropAllQueryImpl
import org.komapper.r2dbc.dsl.query.SchemaDropQuery
import org.komapper.r2dbc.dsl.query.SchemaDropQueryImpl

object R2dbcSchemaDsl {

    fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(metamodels)
    }

    fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return create(metamodels.toList())
    }

    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(metamodels)
    }

    fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return drop(metamodels.toList())
    }

    fun dropAll(): SchemaDropAllQuery {
        return SchemaDropAllQueryImpl()
    }
}
