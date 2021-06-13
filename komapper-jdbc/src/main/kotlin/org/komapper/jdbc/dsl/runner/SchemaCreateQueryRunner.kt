package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaCreateOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class SchemaCreateQueryRunner(
    private val entityMetamodels: List<EntityMetamodel<*, *, *>>,
    private val options: SchemaCreateOptions
) : JdbcQueryRunner<Unit> {

    override fun run(config: JdbcDatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
