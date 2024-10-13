package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.runner.ScriptExecuteRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class R2dbcScriptExecuteRunner(
    private val context: ScriptContext,
) :
    R2dbcRunner<Unit> {
    private val runner = ScriptExecuteRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statements = runner.buildStatements()
        val executor = R2dbcExecutor(config, context.options)
        return executor.execute(statements)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
