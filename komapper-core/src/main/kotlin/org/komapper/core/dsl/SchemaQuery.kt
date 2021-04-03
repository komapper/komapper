package org.komapper.core.dsl

import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropAllQueryImpl
import org.komapper.core.dsl.query.SchemaDropQueryImpl
import org.komapper.core.metamodel.EntityMetamodel

object SchemaQuery : Dsl {

    fun create(vararg entityMetamodels: EntityMetamodel<*>): Query<Unit> {
        return SchemaCreateQueryImpl(entityMetamodels.toList())
    }

    fun create(entityMetamodels: List<EntityMetamodel<*>>): Query<Unit> {
        return SchemaCreateQueryImpl(entityMetamodels)
    }

    fun drop(vararg entityMetamodels: EntityMetamodel<*>): Query<Unit> {
        return SchemaDropQueryImpl(entityMetamodels.toList())
    }

    fun drop(entityMetamodels: List<EntityMetamodel<*>>): Query<Unit> {
        return SchemaDropQueryImpl(entityMetamodels)
    }

    fun dropAll(): Query<Unit> {
        return SchemaDropAllQueryImpl()
    }
}
