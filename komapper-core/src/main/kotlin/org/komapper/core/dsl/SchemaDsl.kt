package org.komapper.core.dsl

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.SchemaCreateQuery
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropAllQuery
import org.komapper.core.dsl.query.SchemaDropAllQueryImpl
import org.komapper.core.dsl.query.SchemaDropQuery
import org.komapper.core.dsl.query.SchemaDropQueryImpl

object SchemaDsl : Dsl {

    fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(metamodels.toList())
    }

    fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(metamodels)
    }

    fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return SchemaDropQueryImpl(metamodels.toList())
    }

    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(metamodels)
    }

    fun dropAll(): SchemaDropAllQuery {
        return SchemaDropAllQueryImpl()
    }
}
