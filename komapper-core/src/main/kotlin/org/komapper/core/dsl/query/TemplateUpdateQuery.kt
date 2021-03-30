package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.TemplateUpdateOptions
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.TemplateUpdateOptionsDeclaration
import org.komapper.core.dsl.scope.TemplateUpdateOptionsScope
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateUpdateQuery : Query<Int> {
    fun options(declaration: TemplateUpdateOptionsDeclaration): TemplateUpdateQuery
}

internal data class TemplateUpdateQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val options: TemplateUpdateOptions = OptionsImpl()
) : TemplateUpdateQuery {

    override fun options(declaration: TemplateUpdateOptionsDeclaration): TemplateUpdateQueryImpl {
        val scope = TemplateUpdateOptionsScope(options)
        declaration(scope)
        return copy(options = scope.options)
    }

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, options)
        return executor.executeUpdate(statement) { _, count -> count }
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
