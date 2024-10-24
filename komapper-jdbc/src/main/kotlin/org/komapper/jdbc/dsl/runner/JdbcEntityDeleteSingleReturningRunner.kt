package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityDeleteSingleReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcEntityDeleteSingleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
) : JdbcRunner<T?> {
    private val runner: EntityDeleteSingleReturningRunner<ENTITY, ID, META> =
        EntityDeleteSingleReturningRunner(context, entity)

    private val support: JdbcEntityDeleteReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityDeleteReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): T? {
        val result = delete(config)
        postDelete(entity, result.size.toLong())
        return result.singleOrNull()
    }

    private fun delete(config: JdbcDatabaseConfig): List<T?> {
        val statement = runner.buildStatement(config)
        return support.delete(config) { executor ->
            executor.executeReturning(statement, transform) { flow ->
                flow.take(1).toList()
            }
        }
    }

    private fun postDelete(entity: ENTITY, count: Long) {
        runner.postDelete(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
