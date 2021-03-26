package org.komapper.core.query.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.OptimisticLockException
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.EntityMetamodel

internal class EntityDeleteCommand<ENTITY>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val entity: ENTITY,
    config: DatabaseConfig,
    private val statement: Statement
) : Command<Unit> {

    private val executor: Executor = Executor(config)

    override fun execute() {
        executor.executeUpdate(statement) { _, count ->
            if (entityMetamodel.versionProperty() != null && count != 1) {
                throw OptimisticLockException()
            }
            entity
        }
    }
}
