package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal class SetOperationFlowBuilder<T>(
    private val context: SetOperationContext,
    private val options: SelectOptions,
    private val transform: (R2dbcDialect, Row) -> T
) : FlowBuilder<T> {

    private val runner: SetOperationRunner = SetOperationRunner(context, options)

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
            is SetOperationContext -> {
                checkWhereClauses(subqueryContext.left)
                checkWhereClauses(subqueryContext.right)
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
