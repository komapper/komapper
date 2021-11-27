package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpsertSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : Runner {

    private val support: EntityUpsertRunnerSupport<ENTITY, ID, META> =
        EntityUpsertRunnerSupport(context)

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entity)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }
}
