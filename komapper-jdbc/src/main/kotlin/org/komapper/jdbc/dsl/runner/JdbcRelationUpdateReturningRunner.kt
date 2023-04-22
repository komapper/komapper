package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationUpdateReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal class JdbcRelationUpdateReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
) : JdbcRunner<List<T>> {

    private val runner: RelationUpdateReturningRunner<ENTITY, ID, META> = RelationUpdateReturningRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<T> {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        val result = runner.buildStatement(config, updatedAtAssignment)
        val statement = result.getOrNull()
        return if (statement != null) {
            val executor = JdbcExecutor(config, context.options)
            executor.executeQuery(statement, transform) { it.toList() }
        } else {
            emptyList()
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
