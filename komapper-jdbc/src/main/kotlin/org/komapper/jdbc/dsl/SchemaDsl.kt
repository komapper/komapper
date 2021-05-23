package org.komapper.jdbc.dsl

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.dsl.query.SchemaCreateQuery
import org.komapper.jdbc.dsl.query.SchemaCreateQueryImpl
import org.komapper.jdbc.dsl.query.SchemaDropAllQuery
import org.komapper.jdbc.dsl.query.SchemaDropAllQueryImpl
import org.komapper.jdbc.dsl.query.SchemaDropQuery
import org.komapper.jdbc.dsl.query.SchemaDropQueryImpl

object SchemaDsl : Dsl {

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
