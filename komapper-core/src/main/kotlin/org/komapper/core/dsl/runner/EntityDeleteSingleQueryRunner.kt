package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions

class EntityDeleteSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    options: EntityDeleteOptions,
    private val entity: ENTITY
) : QueryRunner {

    private val support: EntityDeleteQueryRunnerSupport<ENTITY, ID, META> =
        EntityDeleteQueryRunnerSupport(context, options)

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        return support.buildStatement(config, entity)
    }
}
