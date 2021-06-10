package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateSelectOption
import org.komapper.core.dsl.query.Row
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class TemplateSelectQueryRunner<T, R>(
    private val sql: String,
    private val params: Any,
    private val provide: (Row) -> T,
    private val option: TemplateSelectOption,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcQueryRunner<R> {

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(
            statement,
            { dialect, row ->
                provide(R2dbcRow(dialect, row))
            },
            collect
        )
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).asSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
    }
}
