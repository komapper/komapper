package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityInsertBatchRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : Runner {
    override fun check(config: DatabaseConfig) {
        checkBatchExecutionOfParameterizedStatement(config)
        checkBatchExecutionReturningGeneratedValues(config, context.target)
    }

    private val support: EntityInsertRunnerSupport<ENTITY, ID, META> =
        EntityInsertRunnerSupport(context)

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        if (entities.isEmpty()) return DryRunStatement.EMPTY
        val statement = buildStatement(config, entities.first())
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }

    fun postInsert(entities: List<ENTITY>, generatedKeys: List<Long?>): List<ENTITY> {
        val iterator = generatedKeys.iterator()
        return entities.map { entity ->
            if (iterator.hasNext()) {
                val generatedKey = iterator.next()
                if (generatedKey != null) {
                    support.postInsert(entity, generatedKey)
                } else {
                    entity
                }
            } else {
                entity
            }
        }
    }
}
