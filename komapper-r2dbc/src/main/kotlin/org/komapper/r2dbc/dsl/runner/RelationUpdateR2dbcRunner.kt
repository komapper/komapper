package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.runner.RelationUpdateRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class RelationUpdateR2dbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
    private val options: UpdateOptions
) : R2dbcRunner<Int> {

    private val runner: RelationUpdateRunner<ENTITY, ID, META> = RelationUpdateRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        val statement = runner.buildStatement(config, updatedAtAssignment)
        val executor = R2dbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
