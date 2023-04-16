package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateSingleReturningRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpdateSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : JdbcRunner<ENTITY?> {

    private val runner: EntityUpdateSingleReturningRunner<ENTITY, ID, META> =
        EntityUpdateSingleReturningRunner(context, entity)

    private val support: JdbcEntityUpdateReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpdateReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): ENTITY? {
        val newEntity = preUpdate(config, entity)
        val entity = update(config, newEntity)
        return postUpdate(entity)
    }

    private fun preUpdate(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return runner.preUpdate(config, entity)
    }

    private fun update(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY? {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { executor ->
            val transform = JdbcResultSetTransformers.singleEntity(context.target)
            executor.execute(statement, transform) { it.firstOrNull() }
        }
    }

    private fun postUpdate(entity: ENTITY?): ENTITY? {
        return runner.postUpdate(entity)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
