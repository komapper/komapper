package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.runner.ScriptExecuteRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcScriptExecuteRunner(
    private val context: ScriptContext,
) :
    JdbcRunner<Unit> {
    private val runner = ScriptExecuteRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig) {
        val statements = runner.buildStatements()
        val executor = config.dialect.createExecutor(config, context.options)
        return executor.execute(statements)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
