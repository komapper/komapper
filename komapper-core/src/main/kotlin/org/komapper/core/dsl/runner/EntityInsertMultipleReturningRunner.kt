package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityInsertMultipleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    entities: List<ENTITY>,
) : Runner {

    private val runner: EntityInsertMultipleRunner<ENTITY, ID, META> = EntityInsertMultipleRunner(context, entities)

    override fun check(config: DatabaseConfig) {
        checkInsertMultipleReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        return runner.buildStatement(config, entities)
    }
}
