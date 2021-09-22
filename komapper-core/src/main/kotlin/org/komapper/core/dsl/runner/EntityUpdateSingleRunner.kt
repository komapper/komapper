package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityUpdateOptions

class EntityUpdateSingleRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    options: EntityUpdateOptions,
    private val entity: ENTITY
) : Runner {

    private val support: EntityUpdateRunnerSupport<ENTITY, ID, META> =
        EntityUpdateRunnerSupport(context, options)

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entity)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
