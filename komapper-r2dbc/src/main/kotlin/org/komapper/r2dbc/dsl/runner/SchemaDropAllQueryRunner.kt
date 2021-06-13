package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SchemaDropAllQueryRunner(
    private val options: SchemaDropAllOptions
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
        return builder.dropAll()
    }
}
