package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class RelationDeleteReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: RelationDeleteContext<ENTITY, ID, META>,
) : Runner {

    private val runner: RelationDeleteRunner<ENTITY, ID, META> = RelationDeleteRunner(context)

    override fun check(config: DatabaseConfig) {
        checkDeleteReturning(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }

    fun buildStatement(config: DatabaseConfig): Statement {
        return runner.buildStatement(config)
    }
}
