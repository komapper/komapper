package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.TemplateUpdateOption

interface TemplateExecuteQuery : Query<Int> {
    fun option(configurator: QueryOptionConfigurator<TemplateUpdateOption>): TemplateExecuteQuery
}

internal data class TemplateExecuteQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateUpdateOption = TemplateUpdateOption()
) : TemplateExecuteQuery {

    override fun option(configurator: QueryOptionConfigurator<TemplateUpdateOption>): TemplateExecuteQueryImpl {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option.asJdbcOption())
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, params)
    }
}
