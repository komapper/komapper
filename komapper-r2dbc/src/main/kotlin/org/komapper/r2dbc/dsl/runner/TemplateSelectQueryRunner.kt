package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateSelectOption
import org.komapper.core.dsl.query.Row
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class TemplateSelectQueryRunner<T, R>(
    private val sql: String,
    private val params: Any,
    private val transform: (Row) -> T,
    private val option: TemplateSelectOption,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcQueryRunner<R> {

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        val flow = executor.executeQuery(statement) { dialect, row ->
            transform(R2dbcRowWrapper(dialect, row))
        }
        return collect(flow)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
    }
}
