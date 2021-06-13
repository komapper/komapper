package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.ScriptExecuteOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class ScriptExecuteQueryRunner(
    sql: String,
    private val options: ScriptExecuteOptions
) :
    JdbcQueryRunner<Unit> {
    private val statement = Statement(sql)

    override fun run(config: JdbcDatabaseConfig) {
        val executor = JdbcExecutor(config, options)
        return executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return statement
    }
}
