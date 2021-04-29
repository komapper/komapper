package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption

interface SchemaCreateQuery : Query<Unit> {
    fun option(configurator: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaCreateOption = SchemaCreateOption.default
) : SchemaCreateQuery {

    override fun option(configurator: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery {
        return copy(option = configurator(option))
    }

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
