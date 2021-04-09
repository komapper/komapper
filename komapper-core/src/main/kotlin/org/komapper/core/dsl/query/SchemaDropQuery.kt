package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.config.JdbcOption
import org.komapper.core.data.Statement
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.jdbc.JdbcExecutor

interface SchemaDropQuery : Query<Unit> {
    fun drop(): SchemaDropQuery
}

internal data class SchemaDropQueryImpl(
    val entityMetamodels: List<EntityMetamodel<*>> = emptyList()
) : SchemaDropQuery {

    override fun drop(): SchemaDropQueryImpl {
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
        val builder = dialect.schemaStatementBuilder
        return builder.drop(entityMetamodels)
    }
}
