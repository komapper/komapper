package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.runner.SchemaDropAllRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SchemaDropAllR2dbcRunner(
    private val context: SchemaContext,
) : R2dbcRunner<Unit> {

    private val runner = SchemaDropAllRunner(context)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statements = runner.buildStatements(config)
        val executor = R2dbcExecutor(config, context.options)
        executor.execute(statements)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
