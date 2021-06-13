package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaCreateOptions

class SchemaCreateQueryRunner(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>>,
    @Suppress("unused") private val options: SchemaCreateOptions
) : QueryRunner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
