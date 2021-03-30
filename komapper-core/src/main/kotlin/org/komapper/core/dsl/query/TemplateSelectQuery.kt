package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.TemplateSelectOptions
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.TemplateSelectOptionsDeclaration
import org.komapper.core.dsl.scope.TemplateSelectOptionsScope
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.template.DefaultStatementBuilder

interface TemplateSelectQuery<T> : ListQuery<T> {
    fun options(declaration: TemplateSelectOptionsDeclaration): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T,
    private val options: TemplateSelectOptions = OptionsImpl()
) : TemplateSelectQuery<T> {

    override fun options(declaration: TemplateSelectOptionsDeclaration): TemplateSelectQueryImpl<T> {
        val scope = TemplateSelectOptionsScope(options)
        declaration(scope)
        return copy(options = scope.options)
    }

    override fun run(config: DatabaseConfig): List<T> {
        val terminal = Terminal { it.toList() }
        return terminal.run(config)
    }

    override fun toStatement(dialect: Dialect): Statement {
        val terminal = Terminal { it.toList() }
        return terminal.toStatement(dialect)
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
            val statement = toStatement(config.dialect)
            val executor = JdbcExecutor(config, options)
            return executor.executeQuery(
                statement,
                { dialect, rs ->
                    val row = Row(dialect, rs)
                    provider(row)
                },
                transformer
            )
        }

        override fun toStatement(dialect: Dialect): Statement {
            val builder = DefaultStatementBuilder(
                dialect::formatValue,
                dialect.sqlNodeFactory,
                dialect.exprEvaluator
            )
            return builder.build(sql, params)
        }
    }
}
