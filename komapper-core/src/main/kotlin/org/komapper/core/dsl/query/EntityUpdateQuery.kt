package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.jdbc.JdbcExecutor

internal data class EntityUpdateQuery<ENTITY>(
    private val context: EntityUpdateContext<ENTITY>,
    private val entity: ENTITY
) :
    Query<ENTITY> {

    override fun run(config: DatabaseConfig): ENTITY {
        val clock = config.clockProvider.now()
        val newEntity = context.entityMetamodel.updateUpdatedAt(entity, clock)
        val statement = buildStatement(config.dialect, newEntity)
        val executor = JdbcExecutor(config)
        return executor.executeUpdate(statement) { _, count ->
            if (context.entityMetamodel.versionProperty() != null && count != 1) {
                throw OptimisticLockException()
            }
            context.entityMetamodel.incrementVersion(newEntity)
        }
    }

    override fun toStatement(dialect: Dialect): Statement {
        return buildStatement(dialect, entity)
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(dialect, context, entity)
        return builder.build()
    }
}
