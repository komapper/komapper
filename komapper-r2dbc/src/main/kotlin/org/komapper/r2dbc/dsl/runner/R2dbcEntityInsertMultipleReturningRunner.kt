package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertMultipleReturningRunner
import org.komapper.r2dbc.R2dbcDataOperator
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class R2dbcEntityInsertMultipleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val transform: (R2dbcDataOperator, Row) -> T,
) :
    R2dbcRunner<List<T>> {

    private val runner: EntityInsertMultipleReturningRunner<ENTITY, ID, META> =
        EntityInsertMultipleReturningRunner(context, entities)

    private val support: R2dbcEntityInsertReturningRunnerSupport<ENTITY, ID, META> =
        R2dbcEntityInsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override suspend fun run(config: R2dbcDatabaseConfig): List<T> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        return insert(config, newEntities)
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): List<T> {
        val statement = runner.buildStatement(config, entities)
        return support.insert(config) { executor ->
            val flow = executor.executeQuery(statement, transform)
            flow.toList()
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
