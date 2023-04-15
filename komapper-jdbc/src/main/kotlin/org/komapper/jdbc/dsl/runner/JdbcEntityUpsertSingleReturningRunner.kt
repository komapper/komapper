package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertSingleReturningRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : JdbcRunner<ENTITY?> {

    private val runner: EntityUpsertSingleReturningRunner<ENTITY, ID, META> =
        EntityUpsertSingleReturningRunner(context, entity)

    private val support: JdbcEntityUpsertReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): ENTITY? {
        val newEntity = preUpsert(config, entity)
        return upsert(config, newEntity)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY? {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config) { executor ->
            val transform = JdbcResultSetTransformers.singleEntity(context.target)
            executor.execute(statement, transform) { it.singleOrNull() }
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
