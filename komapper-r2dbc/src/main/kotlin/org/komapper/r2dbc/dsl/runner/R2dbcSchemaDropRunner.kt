package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.runner.SchemaDropRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcSchemaDropRunner(
    private val context: SchemaContext,
) : R2dbcRunner<Unit> {
    private val runner = SchemaDropRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statements = runner.buildStatements(config)
        val executor = R2dbcExecutor(config, context.options)
        executor.execute(statements) {
            val dialect = config.dialect
            val exception = it.exception()
            !dialect.isTableNotExistsError(exception) &&
                !dialect.isSequenceNotExistsError(exception)
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
