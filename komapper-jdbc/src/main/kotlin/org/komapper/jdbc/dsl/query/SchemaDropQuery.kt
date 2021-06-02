package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SchemaDropOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcExecutor

interface SchemaDropQuery : Query<Unit> {
    fun option(configure: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery
}

internal data class SchemaDropQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*, *, *>> = emptyList(),
    val option: SchemaDropOption = SchemaDropOption.default
) : SchemaDropQuery {

    override fun option(configure: (SchemaDropOption) -> SchemaDropOption): SchemaDropQuery {
        return copy(option = configure(option))
    }

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
        return builder.drop(entityMetamodels)
    }
}
