package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.TemplateUpdateOptionDeclaration
import org.komapper.core.dsl.scope.TemplateUpdateOptionScope
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateUpdateQuery : Query<Int> {
    fun option(declaration: TemplateUpdateOptionDeclaration): TemplateUpdateQuery
}

internal data class TemplateUpdateQueryImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateUpdateOption = QueryOptionImpl()
) : TemplateUpdateQuery {

    override fun option(declaration: TemplateUpdateOptionDeclaration): TemplateUpdateQueryImpl {
        val scope = TemplateUpdateOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, option.asJdbcOption())
        val (count) = executor.executeUpdate(statement)
        return count
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
