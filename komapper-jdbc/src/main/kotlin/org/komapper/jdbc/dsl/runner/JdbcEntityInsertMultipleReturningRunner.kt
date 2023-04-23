package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityInsertMultipleReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcEntityInsertMultipleReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
) :
    JdbcRunner<List<T>> {

    private val runner: EntityInsertMultipleReturningRunner<ENTITY, ID, META> =
        EntityInsertMultipleReturningRunner(context, entities)

    private val support: JdbcEntityInsertReturningRunnerSupport<ENTITY, ID, META> =
        JdbcEntityInsertReturningRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<T> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        return insert(config, newEntities)
    }

    private fun preInsert(config: JdbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: JdbcDatabaseConfig, entities: List<ENTITY>): List<T> {
        val statement = runner.buildStatement(config, entities)
        return support.insert(config) { executor ->
            executor.executeReturning(statement, transform) { it.toList() }
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
