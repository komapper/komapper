package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.JdbcExecutor
import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaDropOption

interface SchemaDropQuery : Query<Unit> {
    fun option(configurator: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery
}

internal data class SchemaDropQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaDropOption = SchemaDropOption.default
) : SchemaDropQuery {

    override fun option(configurator: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery {
        return copy(option = configurator(option))
    }

    override fun run(holder: DatabaseConfigHolder) {
        val config = holder.config
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        executor.execute(statement)
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.getSchemaStatementBuilder()
        return builder.drop(entityMetamodels)
    }
}
