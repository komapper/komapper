package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.core.dsl.runner.SchemaDropAllQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcSchemaDropAllQueryRunner(
    private val options: SchemaDropAllOptions
) : JdbcQueryRunner<Unit> {

    private val runner = SchemaDropAllQueryRunner(options)

    override fun run(config: JdbcDatabaseConfig) {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
