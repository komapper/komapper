package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationDeleteReturningRunner
import org.komapper.jdbc.JdbcDataOperator
import org.komapper.jdbc.JdbcDatabaseConfig
import java.sql.ResultSet

internal class JdbcRelationDeleteReturningRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>, T>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
    private val transform: (JdbcDataOperator, ResultSet) -> T,
) : JdbcRunner<List<T>> {
    private val runner: RelationDeleteReturningRunner<ENTITY, ID, META> = RelationDeleteReturningRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): List<T> {
        val statement = runner.buildStatement(config)
        val executor = config.dialect.createExecutor(config, context.options)
        return executor.executeReturning(statement, transform) { it.toList() }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
