package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.runner.TemplateExecuteQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcTemplateExecuteQueryRunner(
    private val sql: String,
    private val params: Any = object {},
    private val options: TemplateExecuteOptions
) : JdbcQueryRunner<Int> {

    private val runner = TemplateExecuteQueryRunner(sql, params, options)

    override fun run(config: JdbcDatabaseConfig): Int {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
