package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.JdbcOption
import org.komapper.core.data.Statement

interface SchemaDropAllQuery : Query<Unit> {
    fun dropAll(): SchemaDropAllQuery
}

internal class SchemaDropAllQueryImpl : SchemaDropAllQuery {

    override fun dropAll(): SchemaDropAllQueryImpl {
        return this
    }

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, JdbcOption())
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.dropAll()
    }
}
