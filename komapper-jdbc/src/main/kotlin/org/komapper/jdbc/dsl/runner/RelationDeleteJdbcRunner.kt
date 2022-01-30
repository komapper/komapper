package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationDeleteRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class RelationDeleteJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : JdbcRunner<Int> {

    private val runner: RelationDeleteRunner<ENTITY, ID, META> = RelationDeleteRunner(context)

    override fun run(config: JdbcDatabaseConfig): Int {
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, context.options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
