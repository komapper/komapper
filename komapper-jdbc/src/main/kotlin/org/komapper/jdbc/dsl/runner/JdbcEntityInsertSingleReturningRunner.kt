package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertSingleReturningRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityInsertSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : JdbcRunner<ENTITY> {

    private val runner: EntityInsertSingleReturningRunner<ENTITY, ID, META> =
        EntityInsertSingleReturningRunner(context, entity)

    private val support: JdbcEntityInsertReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        return insert(config, newEntity)
    }

    private fun preInsert(config: JdbcDatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private fun insert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        val statement = runner.buildStatement(config, entity)
        return support.insert(config) { executor ->
            val transform = JdbcResultSetTransformers.singleEntity(context.target)
            executor.execute(statement, transform) { it.first() }
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
