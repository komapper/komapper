package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateSelectOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

interface TemplateSelectQuery<T> : ListQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any,
    private val provide: (Row) -> T,
    private val option: TemplateSelectOption
) : TemplateSelectQuery<T> {

    override fun run(config: DatabaseConfig): List<T> {
        val terminal = Terminal { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(config: DatabaseConfig): String {
        val terminal = Terminal { it.toList() }
        return terminal.dryRun(config)
    }

    override fun first(): Query<T> {
        return Terminal { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Terminal { it.firstOrNull() }
    }

    override fun <R> collect(transform: (Sequence<T>) -> R): Query<R> {
        return Terminal(transform)
    }

    private inner class Terminal<R>(val transform: (Sequence<T>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option)
            return executor.executeQuery(
                statement,
                { dialect, rs ->
                    val row = RowImpl(dialect, rs)
                    provide(row)
                },
                transform
            )
        }

        override fun dryRun(config: DatabaseConfig): String {
            return buildStatement(config).sql
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = config.templateStatementBuilder
            return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
        }
    }
}
