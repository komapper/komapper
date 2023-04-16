package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.single
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertSingleReturningRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityInsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : R2dbcRunner<ENTITY> {

    private val runner: EntityInsertSingleReturningRunner<ENTITY, ID, META> =
        EntityInsertSingleReturningRunner(context, entity)

    private val support: R2dbcEntityInsertReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityInsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        return insert(config, newEntity)
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        val statement = runner.buildStatement(config, entity)
        return support.insert(config) { executor ->
            val transform = R2dbcRowTransformers.singleEntity(context.target)
            val flow = executor.executeQuery(statement, transform)
            flow.single()
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
