package org.komapper.core.dsl

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.SchemaCreateQuery
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropAllQuery
import org.komapper.core.dsl.query.SchemaDropAllQueryImpl
import org.komapper.core.dsl.query.SchemaDropQuery
import org.komapper.core.dsl.query.SchemaDropQueryImpl

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
