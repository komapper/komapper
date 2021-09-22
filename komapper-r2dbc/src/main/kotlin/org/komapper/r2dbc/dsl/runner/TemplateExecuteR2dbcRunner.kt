package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.runner.TemplateExecuteRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class TemplateExecuteR2dbcRunner(
    sql: String,
    data: Any,
    private val options: TemplateExecuteOptions
) : R2dbcRunner<Int> {

    private val runner = TemplateExecuteRunner(sql, data, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
