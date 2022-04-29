package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpdateBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    Runner {

    private val support: EntityUpdateRunnerSupport<ENTITY, ID, META> =
        EntityUpdateRunnerSupport(context)

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

    fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    fun postUpdate(entities: List<ENTITY>, counts: List<Long>): List<ENTITY> {
        val iterator = counts.iterator()
        return entities.mapIndexed { index, entity ->
            val count = if (iterator.hasNext()) {
                iterator.next()
            } else {
                error("Count value is not found. index=$index")
            }
            support.postUpdate(entity, count, index)
        }
    }
}
