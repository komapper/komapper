package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.SqlExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaCreateOption

interface SchemaCreateQuery : Query<Unit> {
    fun option(configurator: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaCreateOption = SchemaCreateOption()
) : SchemaCreateQuery {

    override fun option(configurator: (SchemaCreateOption) -> SchemaCreateOption): SchemaCreateQuery {
        return copy(option = configurator(option))
    }

    override fun run(holder: DatabaseConfigHolder) {
        val config = holder.config
        val statement = buildStatement(config)
        val executor = SqlExecutor(config, SchemaCreateOption())
        executor.execute(statement)
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
