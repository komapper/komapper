package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateExecuteOption

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

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
    }
}
