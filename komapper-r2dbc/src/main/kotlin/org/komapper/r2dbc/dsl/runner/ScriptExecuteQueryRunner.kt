package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.ScriptExecuteOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class ScriptExecuteQueryRunner(
    private val sql: String,
    private val options: ScriptExecuteOptions = ScriptExecuteOptions.default
) :
    R2dbcQueryRunner<Unit> {

    private val statement = Statement(sql)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val executor = R2dbcExecutor(config, options)
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return statement
    }
}
