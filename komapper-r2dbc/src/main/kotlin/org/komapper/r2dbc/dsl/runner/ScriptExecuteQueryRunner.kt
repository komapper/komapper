package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.option.ScriptExecuteOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class ScriptExecuteQueryRunner(
    val sql: String,
    val option: ScriptExecuteOption = ScriptExecuteOption.default
) :
    R2dbcQueryRunner<Unit> {

    private val statement = Statement(sql)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val executor = R2dbcExecutor(config, option)
        return executor.execute(statement)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return statement.toSql()
    }
}
