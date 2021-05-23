package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.ScriptExecuteOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

interface ScriptExecuteQuery : Query<Unit> {
    fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQuery
}

internal data class ScriptExecuteQueryImpl(
    val sql: String,
    val option: ScriptExecuteOption = ScriptExecuteOption.default
) :
    ScriptExecuteQuery {
    private val statement = Statement(sql, emptyList(), sql)

    override fun option(configure: (ScriptExecuteOption) -> ScriptExecuteOption): ScriptExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, option)
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return statement.sql
    }
}
