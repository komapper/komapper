package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.scope.TemplateSelectOptionDeclaration
import org.komapper.core.dsl.scope.TemplateSelectOptionScope

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

    override fun dryRun(config: DatabaseConfig): Statement {
        val terminal = Terminal { it.toList() }
        return terminal.dryRun(config)
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
            val statement = buildStatement(config)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(
                statement,
                { dialect, rs ->
                    val row = RowImpl(dialect, rs)
                    provider(row)
                },
                transformer
            )
        }

        override fun dryRun(config: DatabaseConfig): Statement {
            return buildStatement(config)
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = config.templateStatementBuilder
            return builder.build(sql, params)
        }
    }
}
