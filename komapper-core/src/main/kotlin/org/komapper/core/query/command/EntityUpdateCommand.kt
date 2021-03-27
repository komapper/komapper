package org.komapper.core.query.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.JdbcExecutor
import org.komapper.core.metamodel.EntityMetamodel

internal class EntityUpdateCommand<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY,
    private val config: DatabaseConfig,
    private val statementBuilder: (Dialect, ENTITY) -> Statement
) : Command<ENTITY> {

    private val executor: JdbcExecutor = JdbcExecutor(config)

    override fun execute(): ENTITY {
        val clock = config.clockProvider.now()
        val newEntity = entityMetamodel.updateUpdatedAt(entity, clock)
        val statement = statementBuilder(config.dialect, newEntity)
        return executor.executeUpdate(statement) { _, count ->
            if (entityMetamodel.versionProperty() != null && count != 1) {
                throw OptimisticLockException()
            }
            entityMetamodel.incrementVersion(newEntity)
        }
    }
}
