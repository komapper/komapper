package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaCreateOptions
import org.komapper.core.dsl.runner.SchemaCreateQueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcSchemaCreateQueryRunner(
    entityMetamodels: List<EntityMetamodel<*, *, *>>,
    private val options: SchemaCreateOptions
) : R2dbcQueryRunner<Unit> {

    private val runner = SchemaCreateQueryRunner(entityMetamodels, options)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
