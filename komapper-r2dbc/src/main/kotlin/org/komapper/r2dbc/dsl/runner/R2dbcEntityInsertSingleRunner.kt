package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityInsertSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : R2dbcRunner<ENTITY> {

    private val runner: EntityInsertSingleRunner<ENTITY, ID, META> =
        EntityInsertSingleRunner(context, entity)

    private val support: R2dbcEntityInsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityInsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        val (_, generatedKeys) = insert(config, newEntity)
        return postInsert(newEntity, generatedKeys)
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Long, List<Long>> {
        val statement = runner.buildStatement(config, entity)
        return support.insert(config, true) { it.executeUpdate(statement) }
    }

    private fun postInsert(entity: ENTITY, generatedKeys: List<Long>): ENTITY {
        return runner.postInsert(entity, generatedKeys)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
