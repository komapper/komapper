package org.komapper.core.dsl.builder

import org.komapper.core.data.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface SchemaStatementBuilder {
    fun create(entityMetamodels: List<EntityMetamodel<*>>): Statement
    fun drop(entityMetamodels: List<EntityMetamodel<*>>): Statement
    fun dropAll(): Statement
}
