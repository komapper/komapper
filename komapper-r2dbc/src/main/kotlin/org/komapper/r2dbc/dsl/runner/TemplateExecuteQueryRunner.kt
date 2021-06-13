package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class TemplateExecuteQueryRunner(
    private val sql: String,
    private val params: Any = object {},
    private val options: TemplateExecuteOptions = TemplateExecuteOptions.default
) : R2dbcQueryRunner<Int> {

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, options.escapeSequence) }
    }
}
