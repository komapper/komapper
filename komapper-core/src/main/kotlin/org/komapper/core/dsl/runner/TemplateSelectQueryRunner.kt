package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.TemplateSelectOptions

class TemplateSelectQueryRunner(
    private val sql: String,
    private val data: Any,
    private val options: TemplateSelectOptions,
) : QueryRunner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(sql, data) { config.dialect.escape(it, options.escapeSequence) }
    }
}
