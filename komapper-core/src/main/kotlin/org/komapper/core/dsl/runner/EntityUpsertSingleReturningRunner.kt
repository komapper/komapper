package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    entity: ENTITY,
) : Runner {
    private val runner: EntityUpsertSingleRunner<ENTITY, ID, META> = EntityUpsertSingleRunner(context, entity)

    override fun check(config: DatabaseConfig) {
        checkUpsertSingleReturning(config)
        runner.check(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return runner.buildStatement(config, entity)
    }
}
