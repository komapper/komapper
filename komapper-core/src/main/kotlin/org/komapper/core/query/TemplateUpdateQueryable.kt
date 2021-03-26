package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.TemplateUpdateCommand
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateUpdateQueryable : Queryable<Int>
interface TemplateInsertQueryable : Queryable<Int>
interface TemplateDeleteQueryable : Queryable<Int>

internal class TemplateUpdateQueryableImpl(
    private val sql: String,
    private val params: Any = object {}
) : TemplateUpdateQueryable, TemplateInsertQueryable, TemplateDeleteQueryable {

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config)
        val command = TemplateUpdateCommand(config, statement)
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
