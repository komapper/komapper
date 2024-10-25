package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityDeleteSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    entity: ENTITY,
) : Runner {
    private val runner: EntityDeleteSingleRunner<ENTITY, ID, META> =
        EntityDeleteSingleRunner(context, entity)

    override fun check(config: DatabaseConfig) {
        checkDeleteReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        return runner.buildStatement(config)
    }

    fun postDelete(entity: ENTITY, count: Long) {
        runner.postDelete(entity, count)
    }
}
