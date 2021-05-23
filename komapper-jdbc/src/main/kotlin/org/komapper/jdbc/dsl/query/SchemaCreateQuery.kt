package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

interface SchemaCreateQuery : Query<Unit> {
    fun option(configure: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaCreateOption = SchemaCreateOption.default
) : SchemaCreateQuery {

    override fun option(configure: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery {
        return copy(option = configure(option))
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
