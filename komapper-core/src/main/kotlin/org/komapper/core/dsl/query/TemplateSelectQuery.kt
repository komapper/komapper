package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.template.DefaultStatementBuilder

internal data class TemplateSelectQuery<T>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T
) : ListQuery<T> {

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
            val executor = JdbcExecutor(config)
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
