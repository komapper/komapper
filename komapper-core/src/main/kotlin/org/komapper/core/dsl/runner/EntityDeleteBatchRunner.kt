package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityDeleteBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityDeleteContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) :
    Runner {
    private val support: EntityDeleteRunnerSupport<ENTITY, ID, META> =
        EntityDeleteRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        checkBatchExecutionOfParameterizedStatement(config)
        checkOptimisticLockOfBatchExecution(config, context.options)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        if (entities.isEmpty()) return DryRunStatement.EMPTY
        val statement = buildStatement(config, entities.first())
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }

    fun postDelete(counts: List<Long>) {
        for ((i, pair) in entities.zip(counts).withIndex()) {
            val (entity, count) = pair
            support.postDelete(entity, count, i)
        }
    }
}
