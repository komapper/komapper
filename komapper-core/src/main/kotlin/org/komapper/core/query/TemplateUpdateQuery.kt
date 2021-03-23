package org.komapper.core.query

import org.komapper.core.DefaultDatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.TemplateUpdateCommand
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateUpdateQuery : Query<Int>
interface TemplateInsertQuery : Query<Int>
interface TemplateDeleteQuery : Query<Int>

internal class TemplateUpdateQueryImpl(
    private val sql: String,
    private val params: Any = object {}
) : TemplateUpdateQuery, TemplateInsertQuery, TemplateDeleteQuery {

    override fun run(config: DefaultDatabaseConfig): Int {
        val statement = buildStatement(config)
        val command = TemplateUpdateCommand(config, statement)
        return command.execute()
    }

    override fun toStatement(config: DefaultDatabaseConfig): Statement {
        return buildStatement(config)
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
