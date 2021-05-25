package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.option.SchemaDropAllOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

interface SchemaDropAllQuery : Query<Unit> {
    fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery
}

internal data class SchemaDropAllQueryImpl(
    private val option: SchemaDropAllOption = SchemaDropAllOption.default
) : SchemaDropAllQuery {

    override fun option(configure: (SchemaDropAllOption) -> SchemaDropAllOption): SchemaDropAllQuery {
        return copy(option = configure(option))
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        executor.execute(statement)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.dropAll()
    }
}
