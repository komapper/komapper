package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.TemplateSelectOption

interface TemplateSelectQuery<T> : ListQuery<T>

internal data class TemplateSelectQueryImpl<T>(
    private val sql: String,
    private val params: Any,
    private val provider: Row.() -> T,
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

    override fun <R> collect(transformer: (Sequence<T>) -> R): Query<R> {
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

        override fun dryRun(config: DatabaseConfig): String {
            return buildStatement(config).sql
        }

        private fun buildStatement(config: DatabaseConfig): Statement {
            val builder = config.templateStatementBuilder
            return builder.build(sql, params)
        }
    }
}

interface TemplateSelectQueryBuilder {
    fun where(provider: () -> Any): TemplateSelectQueryBuilder
    fun option(configurator: QueryOptionConfigurator<TemplateSelectOption>): TemplateSelectQueryBuilder
    fun <T> select(provider: Row.() -> T): TemplateSelectQuery<T>
}

internal data class TemplateSelectQueryBuilderImpl(
    private val sql: String,
    private val params: Any = object {},
    private val option: TemplateSelectOption = TemplateSelectOption()
) : TemplateSelectQueryBuilder {

    override fun option(configurator: QueryOptionConfigurator<TemplateSelectOption>): TemplateSelectQueryBuilder {
        return copy(option = configurator.apply(option))
    }

    override fun where(provider: () -> Any): TemplateSelectQueryBuilder {
        return copy(params = provider())
    }

    override fun <T> select(provider: Row.() -> T): TemplateSelectQuery<T> {
        return TemplateSelectQueryImpl(sql, params, provider, option)
    }
}
