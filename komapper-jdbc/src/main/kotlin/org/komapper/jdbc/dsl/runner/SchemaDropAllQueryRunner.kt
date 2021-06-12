package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.option.SchemaDropAllOption
import org.komapper.jdbc.DatabaseConfig

internal class SchemaDropAllQueryRunner(
    private val option: SchemaDropAllOption = SchemaDropAllOption.default
) : JdbcQueryRunner<Unit> {

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
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
