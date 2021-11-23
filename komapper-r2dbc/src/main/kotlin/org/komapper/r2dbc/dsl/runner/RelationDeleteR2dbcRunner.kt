package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.runner.RelationDeleteRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class RelationDeleteR2dbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: RelationDeleteContext<ENTITY, ID, META>,
    private val options: DeleteOptions
) : R2dbcRunner<Int> {

    private val runner: RelationDeleteRunner<ENTITY, ID, META> = RelationDeleteRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
