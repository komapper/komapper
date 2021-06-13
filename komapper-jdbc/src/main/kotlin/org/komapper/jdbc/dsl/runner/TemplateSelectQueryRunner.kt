package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.query.Row
import org.komapper.jdbc.DatabaseConfig

internal class TemplateSelectQueryRunner<T, R>(
    private val sql: String,
    private val params: Any,
    private val transform: (Row) -> T,
    private val option: TemplateSelectOptions,
    private val collect: suspend (Flow<T>) -> R,
) : JdbcQueryRunner<R> {

    override fun run(config: DatabaseConfig): R {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        return executor.executeQuery(
            statement,
            { dialect, rs ->
                val row = JdbcResultSetWrapper(dialect, rs)
                transform(row)
            },
            collect
        )
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
    }
}
