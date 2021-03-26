package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.TemplateSelectCommand
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateSelectQueryable<T, R> : Queryable<R>

internal class TemplateSelectQueryableImpl<T, R>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T,
    private val transformer: (Sequence<T>) -> R
) : TemplateSelectQueryable<T, R> {

    override fun run(config: DatabaseConfig): R {
        val statement = buildStatement(config)
        val command = TemplateSelectCommand(config, statement, provider, transformer)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = DefaultStatementBuilder(
            config.dialect::formatValue,
            config.sqlNodeFactory,
            config.exprEvaluator
        )
        return builder.build(sql, params)
    }
}
