package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.runner.SchemaCreateRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class SchemaCreateJdbcRunner(
    entityMetamodels: List<EntityMetamodel<*, *, *>>,
    private val options: SchemaOptions
) : JdbcRunner<Unit> {

    private val runner = SchemaCreateRunner(entityMetamodels, options)

    override fun run(config: JdbcDatabaseConfig) {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
