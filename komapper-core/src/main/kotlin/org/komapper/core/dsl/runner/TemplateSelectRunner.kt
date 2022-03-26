package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DefaultTemplateBuiltinExtensions
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.TemplateSelectContext

class TemplateSelectRunner(
    private val context: TemplateSelectContext,
) : Runner {

    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.templateStatementBuilder
        val extensions = DefaultTemplateBuiltinExtensions {
            config.dialect.escape(it, context.options.escapeSequence)
        }
        return builder.build(context.sql, context.valueMap, extensions)
    }
}
