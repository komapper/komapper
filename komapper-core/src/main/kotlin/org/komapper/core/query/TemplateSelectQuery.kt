package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.TemplateSelectCommand
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateSelectQuery<T, R> : Query<R>

internal class TemplateSelectQueryImpl<T, R>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T,
    private val transformer: (Sequence<T>) -> R
) : TemplateSelectQuery<T, R> {

    override fun run(config: DefaultDatabaseConfig): R {
        val statement = buildStatement(config)
        val command = TemplateSelectCommand(config, statement, provider, transformer)
        return command.execute()
    }

    override fun toSql(config: DefaultDatabaseConfig): String {
        val statement = buildStatement(config)
        return statement.sql
    }

    private fun buildStatement(config: DefaultDatabaseConfig): Statement {
        val builder = DefaultStatementBuilder(
            config.dialect::formatValue,
            config.sqlNodeFactory,
            config.exprEvaluator
        )
        return builder.build(sql, params)
    }
}
