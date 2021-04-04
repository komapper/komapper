package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.TemplateSelectOptionDeclaration
import org.komapper.core.dsl.scope.TemplateSelectOptionScope
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateSelectQuery<T> : ListQuery<T> {
    fun option(declaration: TemplateSelectOptionDeclaration): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T,
    private val option: TemplateSelectOption = QueryOptionImpl()
) : TemplateSelectQuery<T> {

    override fun option(declaration: TemplateSelectOptionDeclaration): TemplateSelectQueryImpl<T> {
        val scope = TemplateSelectOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): List<T> {
        val terminal = Terminal { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(dialect: Dialect): Statement {
        val terminal = Terminal { it.toList() }
        return terminal.dryRun(dialect)
    }

    override fun first(): Query<T> {
        return Terminal { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Terminal { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<T>) -> R): Query<R> {
        return Terminal(transformer)
    }

    private inner class Terminal<R>(val transformer: (Sequence<T>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config.dialect)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(
                statement,
                { dialect, rs ->
                    val row = Row(dialect, rs)
                    provider(row)
                },
                transformer
            )
        }

        override fun dryRun(dialect: Dialect): Statement {
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
}
