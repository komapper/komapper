package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpsertMultipleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>,
) : Runner {

    private val runner: EntityUpsertMultipleRunner<ENTITY, ID, META> = EntityUpsertMultipleRunner(context, entities)

    override fun check(config: DatabaseConfig) {
        checkUpsertMultipleReturning(config)
        runner.check(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        return runner.buildStatement(config, entities)
    }
}
