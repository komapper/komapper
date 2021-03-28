package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.command.TemplateUpdateCommand
import org.komapper.core.template.DefaultStatementBuilder

internal data class TemplateUpdateQuery(
    private val sql: String,
    private val params: Any = object {}
) : Query<Int> {

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config.dialect)
        val command = TemplateUpdateCommand(config, statement)
        return command.execute()
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = DefaultStatementBuilder(
            dialect::formatValue,
            dialect.sqlNodeFactory,
            dialect.exprEvaluator
        )
        return builder.build(sql, params)
    }
}
