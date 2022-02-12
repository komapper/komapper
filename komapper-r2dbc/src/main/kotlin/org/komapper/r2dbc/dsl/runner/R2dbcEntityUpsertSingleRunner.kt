package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpsertSingleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : R2dbcRunner<Int> {

    private val runner: EntityUpsertSingleRunner<ENTITY, ID, META> =
        EntityUpsertSingleRunner(context, entity)

    private val support: R2dbcEntityUpsertRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val newEntity = preUpsert(config, entity)
        val (count) = upsert(config, newEntity)
        return count
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Int, List<Long>> {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config, false) { it.executeUpdate(statement) }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
