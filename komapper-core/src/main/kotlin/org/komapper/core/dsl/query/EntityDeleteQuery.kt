package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityDeleteStatementBuilder
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.jdbc.JdbcExecutor

internal data class EntityDeleteQuery<ENTITY>(
    private val context: EntityDeleteContext<ENTITY>,
    private val entity: ENTITY
) :
    Query<Unit> {

    override fun run(config: DatabaseConfig) {
        val statement = buildStatement(config.dialect)
        val executor = JdbcExecutor(config)
        executor.executeUpdate(statement) { _, count ->
            if (context.entityMetamodel.versionProperty() != null && count != 1) {
                throw OptimisticLockException()
            }
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect)
    }

    private fun buildStatement(dialect: Dialect): Statement {
        val builder = EntityDeleteStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
