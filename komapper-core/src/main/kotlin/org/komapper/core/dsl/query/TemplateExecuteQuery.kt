package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.SqlExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.option.TemplateExecuteOption

interface TemplateExecuteQuery : Query<Int> {
    fun option(configurator: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQuery
    fun params(provider: () -> Any): TemplateExecuteQuery
}

internal data class TemplateExecuteQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateExecuteOption = TemplateExecuteOption()
) : TemplateExecuteQuery {

    override fun option(configurator: (TemplateExecuteOption) -> TemplateExecuteOption): TemplateExecuteQueryImpl {
        return copy(option = configurator(option))
    }

    override fun params(provider: () -> Any): TemplateExecuteQuery {
        return copy(params = provider())
    }

    override fun run(holder: DatabaseConfigHolder): Int {
        val config = holder.config
        val statement = buildStatement(config)
        val executor = SqlExecutor(config, option)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params) { config.dialect.escape(it, option.escapeString) }
    }
}
