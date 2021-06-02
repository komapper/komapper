package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

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

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        executor.execute(statement)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
