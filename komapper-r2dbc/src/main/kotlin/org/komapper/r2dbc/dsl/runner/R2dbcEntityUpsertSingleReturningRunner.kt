package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertSingleReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityUpsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T, R>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val transform: (R2dbcDataOperator, Row) -> T,
    private val collect: suspend (Flow<T>) -> R,
) : R2dbcRunner<R> {
    private val runner: EntityUpsertSingleReturningRunner<ENTITY, ID, META> =
        EntityUpsertSingleReturningRunner(context, entity)

    private val support: R2dbcEntityUpsertReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityUpsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        val newEntity = preUpsert(config, entity)
        return upsert(config, newEntity)
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entity: ENTITY): R {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config, false) { executor ->
            val flow = executor.executeQuery(statement, transform)
            collect(flow)
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
