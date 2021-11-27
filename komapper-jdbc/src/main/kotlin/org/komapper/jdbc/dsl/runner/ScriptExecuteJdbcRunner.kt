package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
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
        val statement = runner.buildStatement()
        val executor = JdbcExecutor(config, context.options)
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
