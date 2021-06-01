package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.ScriptExecuteOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface ScriptExecuteQuery : Query<Unit> {
    fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    val sql: String,
    val option: ScriptExecuteOption = ScriptExecuteOption.default
) :
    ScriptExecuteQuery {
    private val statement = Statement(sql)

    override fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val executor = R2dbcExecutor(config, option)
        return executor.execute(statement)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return statement.toString()
    }
}
