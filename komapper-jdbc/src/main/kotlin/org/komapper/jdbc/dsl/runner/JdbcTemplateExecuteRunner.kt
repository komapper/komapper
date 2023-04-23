package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.runner.TemplateExecuteRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcTemplateExecuteRunner(
    private val context: TemplateExecuteContext,
) : JdbcRunner<Long> {

    private val runner = TemplateExecuteRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): Long {
        val statement = runner.buildStatement(config)
        val executor = config.dialect.createExecutor(config, context.options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
