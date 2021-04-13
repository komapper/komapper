package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.JdbcOption
import org.komapper.core.data.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface SchemaCreateQuery : Query<Unit> {
    fun create(): SchemaCreateQuery
}

internal data class SchemaCreateQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*>> = emptyList()
) : SchemaCreateQuery {

    override fun create(): SchemaCreateQueryImpl {
        return copy(entityMetamodels = entityMetamodels.toList())
    }

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, JdbcOption())
        executor.execute(statement)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = config.dialect.schemaStatementBuilder
        return builder.create(entityMetamodels)
    }
}
