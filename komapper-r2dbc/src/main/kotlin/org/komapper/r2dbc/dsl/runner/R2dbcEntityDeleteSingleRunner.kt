package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityDeleteSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : R2dbcRunner<Unit> {
    private val runner: EntityDeleteSingleRunner<ENTITY, ID, META> =
        EntityDeleteSingleRunner(context, entity)

    private val support: R2dbcEntityDeleteRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityDeleteRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val (count) = delete(config)
        postDelete(entity, count)
    }

    private suspend fun delete(config: R2dbcDatabaseConfig): Pair<Long, List<Long>> {
        val statement = runner.buildStatement(config)
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(entity: ENTITY, count: Long) {
        runner.postDelete(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
