package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class TemplateExecuteQueryRunner(
    private val sql: String,
    private val params: Any = object {},
    private val options: TemplateExecuteOptions
) : JdbcQueryRunner<Int> {

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, options.escapeSequence) }
    }
}
