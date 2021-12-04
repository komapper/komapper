package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationUpdateRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class RelationUpdateR2dbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) : R2dbcRunner<Int> {

    private val runner: RelationUpdateRunner<ENTITY, ID, META> = RelationUpdateRunner(context)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        val result = runner.buildStatement(config, updatedAtAssignment)
        val statement = result.getOrNull()
        return if (statement != null) {
            val executor = R2dbcExecutor(config, context.options)
            val (count) = executor.executeUpdate(statement)
            count
        } else 0
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
