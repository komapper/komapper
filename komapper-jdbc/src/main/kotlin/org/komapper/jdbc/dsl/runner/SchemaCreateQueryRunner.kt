package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal data class SchemaCreateQueryRunner(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaCreateOption = SchemaCreateOption.default
) : JdbcQueryRunner<Unit> {

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
