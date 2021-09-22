package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaDropOptions
import org.komapper.core.dsl.runner.SchemaDropRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class SchemaDropJdbcRunner(
    entityMetamodels: List<EntityMetamodel<*, *, *>>,
    private val options: SchemaDropOptions
) : JdbcRunner<Unit> {

    private val runner = SchemaDropRunner(entityMetamodels, options)

    override fun run(config: JdbcDatabaseConfig) {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
