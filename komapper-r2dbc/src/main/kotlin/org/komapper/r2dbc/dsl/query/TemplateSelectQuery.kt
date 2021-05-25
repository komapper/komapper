package org.komapper.r2dbc.dsl.query

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateSelectOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface TemplateSelectQuery<T> : FlowQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any,
    private val provide: (Row) -> T,
    private val option: TemplateSelectOption
) : TemplateSelectQuery<T> {

    override suspend fun run(config: R2dbcDatabaseConfig): Flow<T> {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(statement) { row, _ ->
            val templateRow = RowImpl(config.dialect, row)
            provide(templateRow)
        }
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
    }
}
