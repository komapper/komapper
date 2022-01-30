package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.runner.ScriptExecuteRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class ScriptExecuteJdbcRunner(
    private val context: ScriptContext,
) :
    JdbcRunner<Unit> {

    private val runner = ScriptExecuteRunner(context)

    override fun run(config: JdbcDatabaseConfig) {
        val statements = runner.buildStatements()
        val executor = JdbcExecutor(config, context.options)
        return executor.execute(statements)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
