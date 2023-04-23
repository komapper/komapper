package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.RelationDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationDeleteRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcRelationDeleteRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationDeleteContext<ENTITY, ID, META>,
) : JdbcRunner<Long> {

    private val runner: RelationDeleteRunner<ENTITY, ID, META> = RelationDeleteRunner(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): Long {
        val statement = runner.buildStatement(config)
        val executor = config.dialect.createExecutor(config, context.options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
