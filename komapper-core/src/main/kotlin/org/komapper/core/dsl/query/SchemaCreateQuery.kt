package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.JdbcOption
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.EntityMetamodel

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
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config, JdbcOption())
        executor.execute(statement)
    }

    override fun dryRun(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
