package org.komapper.core.dsl.builder

import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface SchemaStatementBuilder {
    fun create(entityMetamodels: List<EntityMetamodel<*, *, *>>): Statement
    fun drop(entityMetamodels: List<EntityMetamodel<*, *, *>>): Statement
    fun dropAll(): Statement
}

internal object DryRunSchemaStatementBuilder : SchemaStatementBuilder {

    private val statement = Statement(
        "Not supported to invoke the dryRun function with the default value."
    )

    override fun create(entityMetamodels: List<EntityMetamodel<*, *, *>>) = statement
    override fun drop(entityMetamodels: List<EntityMetamodel<*, *, *>>) = statement
    override fun dropAll() = statement
}
