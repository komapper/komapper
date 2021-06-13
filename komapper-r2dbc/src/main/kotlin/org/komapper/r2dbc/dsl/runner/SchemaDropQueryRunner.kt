package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SchemaDropQueryRunner(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    private val options: SchemaDropOptions = SchemaDropOptions.default
) : R2dbcQueryRunner<Unit> {

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.drop(entityMetamodels)
    }
}
