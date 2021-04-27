package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.JdbcExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.option.TemplateSelectOption

interface TemplateSelectQuery<T> : ListQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any,
    private val provider: Row.() -> T,
    private val option: TemplateSelectOption
) : TemplateSelectQuery<T> {

    override fun run(holder: DatabaseConfigHolder): List<T> {
        val terminal = Terminal { it.toList() }
        return terminal.run(holder)
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val terminal = Terminal { it.toList() }
        return terminal.dryRun(holder)
    }

    override fun first(): Query<T> {
        return Terminal { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Terminal { it.firstOrNull() }
    }

    override fun <R> collect(transformer: (Sequence<T>) -> R): Query<R> {
        return Terminal(transformer)
    }

    private inner class Terminal<R>(val transformer: (Sequence<T>) -> R) : Query<R> {
        override fun run(holder: DatabaseConfigHolder): R {
            val config = holder.config
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option)
            return executor.executeQuery(
                statement,
                { dialect, rs ->
                    val row = RowImpl(dialect, rs)
                    provider(row)
                },
                transformer
            )
        }

        override fun dryRun(holder: DatabaseConfigHolder): String {
            val config = holder.config
            return buildStatement(config).sql
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = config.templateStatementBuilder
            return builder.build(sql, params) { config.dialect.escape(it, option.escapeSequence) }
        }
    }
}
