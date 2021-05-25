package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateExecuteOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface TemplateExecuteQuery : Query<Int> {
    fun option(configure: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQuery
    fun params(provide: () -> Any): TemplateExecuteQuery
}

internal data class TemplateExecuteQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateExecuteOption = TemplateExecuteOption.default
) : TemplateExecuteQuery {

    override fun option(configure: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQueryImpl {
        return copy(option = configure(option))
    }

    override fun params(provide: () -> Any): TemplateExecuteQuery {
        return copy(params = provide())
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
    }
}
