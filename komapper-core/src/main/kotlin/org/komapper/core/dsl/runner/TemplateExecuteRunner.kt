package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.TemplateExecuteContext

class TemplateExecuteRunner(
    private val context: TemplateExecuteContext,
) : Runner {

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        return builder.build(context.sql, context.data) {
            config.dialect.escape(it, context.options.escapeSequence)
        }
    }
}
