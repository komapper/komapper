package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpdateSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    entity: ENTITY,
) : Runner {

    private val runner: EntityUpdateSingleRunner<ENTITY, ID, META> =
        EntityUpdateSingleRunner(context, entity)

    override fun check(config: DatabaseConfig) {
        checkUpdateReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return runner.buildStatement(config, entity)
    }

    fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return runner.preUpdate(config, entity)
    }

    fun postUpdate(result: Any?) {
        if (context.target.versionProperty() != null) {
            val count = if (result == null) 0L else 1L
            checkOptimisticLock(context.options, count, null)
        }
    }
}
