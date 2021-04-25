package org.komapper.core.dsl

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.SchemaCreateQuery
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropAllQuery
import org.komapper.core.dsl.query.SchemaDropAllQueryImpl
import org.komapper.core.dsl.query.SchemaDropQuery
import org.komapper.core.dsl.query.SchemaDropQueryImpl

object SchemaDsl : Dsl {

    fun create(vararg entityMetamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(entityMetamodels.toList())
    }

    fun create(entityMetamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(entityMetamodels)
    }

    fun drop(vararg entityMetamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return SchemaDropQueryImpl(entityMetamodels.toList())
    }

    fun drop(entityMetamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(entityMetamodels)
    }

    fun dropAll(): SchemaDropAllQuery {
        return SchemaDropAllQueryImpl()
    }
}
