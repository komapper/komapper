package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions

class EntityUpsertMultipleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entities: List<ENTITY>
) : QueryRunner {

    private val support: EntityUpsertQueryRunnerSupport<ENTITY, ID, META> =
        EntityUpsertQueryRunnerSupport(context, options)

    override fun dryRun(config: DatabaseConfig): Statement {
        if (entities.isEmpty()) return Statement.EMPTY
        return buildStatement(config, entities)
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        return support.buildStatement(config, entities)
    }
}
