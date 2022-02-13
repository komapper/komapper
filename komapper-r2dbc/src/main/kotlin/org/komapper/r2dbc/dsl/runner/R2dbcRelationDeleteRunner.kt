package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationDeleteRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcRelationDeleteRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : R2dbcRunner<Int> {

    private val runner: RelationDeleteRunner<ENTITY, ID, META> = RelationDeleteRunner(context)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, context.options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
