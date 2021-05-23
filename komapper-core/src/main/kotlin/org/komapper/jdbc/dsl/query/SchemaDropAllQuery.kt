package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.SchemaDropAllOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

interface SchemaDropAllQuery : Query<Unit> {
    fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val option: SchemaDropAllOption = SchemaDropAllOption.default
) : SchemaDropAllQuery {

    override fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery {
        return copy(option = configure(option))
    }

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
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
