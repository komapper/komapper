package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.runner.ScriptExecuteRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class ScriptExecuteR2dbcRunner(
    private val sql: String,
    private val options: ScriptOptions
) :
    R2dbcRunner<Unit> {

    private val runner = ScriptExecuteRunner(sql, options)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = runner.buildStatement()
        val executor = R2dbcExecutor(config, options)
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
