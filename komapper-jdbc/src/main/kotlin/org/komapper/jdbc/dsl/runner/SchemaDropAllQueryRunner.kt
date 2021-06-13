package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.options.SchemaDropAllOptions
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class SchemaDropAllQueryRunner(
    private val options: SchemaDropAllOptions
) : JdbcQueryRunner<Unit> {

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, options)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.dropAll()
    }
}
