package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.option.ScriptExecuteOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal data class ScriptExecuteQueryRunner(
    private val sql: String,
    private val option: ScriptExecuteOption
) :
    JdbcQueryRunner<Unit> {
    private val statement = Statement(sql)

    override fun run(config: DatabaseConfig) {
        val executor = JdbcExecutor(config, option)
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return statement.toSql()
    }
}
