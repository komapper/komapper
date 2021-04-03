package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.JdbcOption
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor

interface SchemaDropAllQuery : Query<Unit> {
    fun dropAll(): SchemaDropAllQuery
}

internal class SchemaDropAllQueryImpl : SchemaDropAllQuery {

    override fun dropAll(): SchemaDropAllQueryImpl {
        return this
    }

    override fun execute(config: DatabaseConfig) {
        val statement = statement(config.dialect)
        val executor = JdbcExecutor(config, JdbcOption())
        executor.execute(statement)
    }

    override fun statement(dialect: Dialect): Statement {
        val builder = dialect.getSchemaStatementBuilder()
        return builder.dropAll()
    }
}
