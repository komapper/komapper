package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.runner.SqlSetOperationRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal class SqlSetOperationFlowBuilder<T>(
    private val context: SqlSetOperationContext,
    private val options: SqlSetOperationOptions,
    private val transform: (R2dbcDialect, Row) -> T
) : FlowBuilder<T> {

    private val runner: SqlSetOperationRunner = SqlSetOperationRunner(context, options)

    override fun build(config: R2dbcDatabaseConfig): Flow<T> {
        if (!options.allowEmptyWhereClause) {
            checkWhereClauses(context.left)
            checkWhereClauses(context.right)
        }
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        return executor.executeQuery(statement, transform)
    }

    private fun checkWhereClauses(subqueryContext: SubqueryContext) {
        when (subqueryContext) {
            is SelectContext<*, *, *> -> {
                if (subqueryContext.where.isEmpty()) {
                    error("Empty where clause is not allowed.")
                }
            }
            is SqlSetOperationContext -> {
                checkWhereClauses(subqueryContext.left)
                checkWhereClauses(subqueryContext.right)
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
