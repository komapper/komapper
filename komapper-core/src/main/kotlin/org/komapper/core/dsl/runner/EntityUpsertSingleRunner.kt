package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel

class EntityUpsertSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : Runner {
    private val support: EntityUpsertRunnerSupport<ENTITY, ID, META> =
        EntityUpsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        checkSearchConditionInUpsertStatement(config, context)
        checkConflictTargetInUpsertStatement(config, context.conflictTarget)
        checkIndexPredicateInUpsertStatement(config, context.indexPredicate)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val statement = buildStatement(config, entity)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }

    fun postUpsert(entity: ENTITY, generatedKeys: List<Long>): ENTITY {
        val key = generatedKeys.firstOrNull()
        return if (key != null) {
            support.postInsert(entity, key)
        } else {
            entity
        }
    }
}
