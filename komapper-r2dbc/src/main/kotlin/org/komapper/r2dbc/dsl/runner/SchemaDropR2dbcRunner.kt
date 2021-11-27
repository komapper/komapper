package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.runner.SchemaDropRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SchemaDropR2dbcRunner(
    private val context: SchemaContext,
) : R2dbcRunner<Unit> {

    private val runner = SchemaDropRunner(context)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, context.options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
