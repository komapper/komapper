package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.runner.TemplateExecuteRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcTemplateExecuteRunner(
    private val context: TemplateExecuteContext,
) : JdbcRunner<Int> {

    private val runner = TemplateExecuteRunner(context)

    override fun run(config: JdbcDatabaseConfig): Int {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, context.options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
