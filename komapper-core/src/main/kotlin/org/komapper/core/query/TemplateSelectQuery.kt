package org.komapper.core.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.query.command.TemplateSelectCommand
import org.komapper.core.template.DefaultStatementBuilder

internal data class TemplateSelectQuery<T>(
    private val sql: String,
    private val params: Any = object {},
    private val provider: Row.() -> T
) : ListQuery<T> {

    override fun run(config: DatabaseConfig): List<T> {
        val transformable = Transformable { it.toList() }
        return transformable.run(config)
    }

    override fun peek(dialect: Dialect): Statement {
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

    override fun first(): Query<T> {
        return Transformable { it.first() }
    }

    override fun firstOrNull(): Query<T?> {
        return Transformable { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<T>) -> R): Query<R> {
        return Transformable(transformer)
    }

    private inner class Transformable<R>(val transformer: (Sequence<T>) -> R) : Query<R> {
        override fun run(config: DatabaseConfig): R {
            val statement = buildStatement(config.dialect)
            val command = TemplateSelectCommand(config, statement, provider, transformer)
            return command.execute()
        }

        override fun peek(dialect: Dialect): Statement = buildStatement(dialect)
    }
}
