package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.option.SchemaDropAllOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class SchemaDropAllQueryRunner(
    private val option: SchemaDropAllOption
) : R2dbcQueryRunner<Unit> {

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        executor.execute(statement)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).asSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.dropAll()
    }
}
