package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.core.dsl.runner.SqlUpdateRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SqlUpdateR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlUpdateContext<ENTITY, ID, META>,
    private val options: SqlUpdateOptions
) : R2dbcRunner<Int> {

    private val runner: SqlUpdateRunner<ENTITY, ID, META> = SqlUpdateRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
