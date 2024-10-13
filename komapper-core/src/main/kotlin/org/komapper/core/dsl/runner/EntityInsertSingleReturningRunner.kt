package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityInsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    entity: ENTITY,
) : Runner {
    private val runner: EntityInsertSingleRunner<ENTITY, ID, META> = EntityInsertSingleRunner(context, entity)

    override fun check(config: DatabaseConfig) {
        checkInsertSingleReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return runner.buildStatement(config, entity)
    }
}
