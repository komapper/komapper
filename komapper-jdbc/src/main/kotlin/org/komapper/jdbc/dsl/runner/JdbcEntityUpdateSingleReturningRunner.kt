package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpdateSingleReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcEntityUpdateSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
) : JdbcRunner<T?> {
    private val runner: EntityUpdateSingleReturningRunner<ENTITY, ID, META> =
        EntityUpdateSingleReturningRunner(context, entity)

    private val support: JdbcEntityUpdateReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpdateReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): T? {
        val newEntity = preUpdate(config, entity)
        val result = update(config, newEntity)
        postUpdate(newEntity, result.size.toLong())
        return result.singleOrNull()
    }

    private fun preUpdate(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return runner.preUpdate(config, entity)
    }

    private fun update(config: JdbcDatabaseConfig, entity: ENTITY): List<T?> {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { executor ->
            executor.executeReturning(statement, transform) { flow ->
                flow.take(1).toList()
            }
        }
    }

    private fun postUpdate(entity: ENTITY, count: Long) {
        runner.postUpdate(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
