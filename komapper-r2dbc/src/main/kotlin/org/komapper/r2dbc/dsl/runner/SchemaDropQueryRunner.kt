package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class SchemaDropQueryRunner(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    private val options: SchemaDropOptions = SchemaDropOptions.default
) : R2dbcQueryRunner<Unit> {

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.drop(entityMetamodels)
    }
}
