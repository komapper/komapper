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

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, JdbcOption())
        executor.execute(statement)
    }

    override fun dryRun(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = dialect.schemaStatementBuilder
        return builder.dropAll()
    }
}
