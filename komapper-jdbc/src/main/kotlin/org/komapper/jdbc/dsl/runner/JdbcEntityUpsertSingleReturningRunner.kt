package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertSingleReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcEntityUpsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T, R>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
    private val collect: suspend (Flow<T>) -> R,
) : JdbcRunner<R> {

    private val runner: EntityUpsertSingleReturningRunner<ENTITY, ID, META> =
        EntityUpsertSingleReturningRunner(context, entity)

    private val support: JdbcEntityUpsertReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): R {
        val newEntity = preUpsert(config, entity)
        return upsert(config, newEntity)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entity: ENTITY): R {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config) { executor ->
            executor.executeQuery(statement, transform, collect)
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
