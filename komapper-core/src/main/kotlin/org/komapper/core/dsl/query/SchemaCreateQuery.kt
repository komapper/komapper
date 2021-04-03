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

    override fun execute(config: DatabaseConfig) {
        val statement = statement(config.dialect)
        val executor = JdbcExecutor(config, JdbcOption())
        executor.execute(statement)
    }

    override fun statement(dialect: Dialect): Statement {
        val builder = dialect.getSchemaStatementBuilder()
        return builder.create(entityMetamodels)
    }
}
