package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityInsertMultipleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>
) :
    Runner {

    override fun check(config: DatabaseConfig) {
        checkAutoIncrementWhenInsertingMultipleRows(config, context.target)
        checkGeneratedKeysReturningWhenInsertingMultipleRows(config, context.target, context.options)
    }

    private val support: EntityInsertRunnerSupport<ENTITY, ID, META> =
        EntityInsertRunnerSupport(context)

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        if (entities.isEmpty()) return DryRunStatement.EMPTY
        val statement = buildStatement(config, entities)
        return DryRunStatement.of(statement, config.dialect)
    }

    fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        return support.buildStatement(config, entities)
    }

    fun postInsert(entities: List<ENTITY>, generatedKeys: List<Long>): List<ENTITY> {
        val iterator = generatedKeys.iterator()
        return entities.map {
            if (iterator.hasNext()) {
                support.postInsert(it, iterator.next())
            } else {
                it
            }
        }
    }
}
