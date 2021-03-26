package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.query.command.TemplateSelectCommand
import org.komapper.core.query.command.TemplateUpdateCommand
import org.komapper.core.template.DefaultStatementBuilder

object TemplateQuery {

    fun <T> select(
        sql: String,
        params: Any = object {},
        provider: Row.() -> T
    ): ListQueryable<T> {
        return TemplateSelectQueryableImpl(sql, params, provider)
    }

    fun update(sql: String, params: Any = object {},): Queryable<Int> {
        return TemplateUpdateQueryableImpl(sql, params)
    }

    fun insert(sql: String, params: Any = object {},): Queryable<Int> {
        return TemplateUpdateQueryableImpl(sql, params)
    }

    fun delete(sql: String, params: Any = object {},): Queryable<Int> {
        return TemplateUpdateQueryableImpl(sql, params)
    }
}

private class TemplateSelectQueryableImpl<T>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T
) : ListQueryable<T> {

    override fun run(config: DatabaseConfig): List<T> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = DefaultStatementBuilder(
            config.dialect::formatValue,
            config.sqlNodeFactory,
            config.exprEvaluator
        )
        return builder.build(sql, params)
    }

    override fun first(): Queryable<T> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Queryable<T?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<T>) -> R): Queryable<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<T>) -> R) : Queryable<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config)
            val command = TemplateSelectCommand(config, statement, provider, transformer)
            return command.execute()
        }

        override fun toStatement(config: DatabaseConfig): Statement = buildStatement(config)
    }
}

private class TemplateUpdateQueryableImpl(
    private val sql: String,
    private val params: Any = object {}
) : Queryable<Int> {

    override fun run(config: DatabaseConfig): Int {
        val statement = buildStatement(config)
        val command = TemplateUpdateCommand(config, statement)
        return command.execute()
    }

    override fun toStatement(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = DefaultStatementBuilder(
            config.dialect::formatValue,
            config.sqlNodeFactory,
            config.exprEvaluator
        )
        return builder.build(sql, params)
    }
}
