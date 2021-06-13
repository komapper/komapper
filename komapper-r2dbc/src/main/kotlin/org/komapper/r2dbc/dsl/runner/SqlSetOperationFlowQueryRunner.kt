package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.SqlSetOperationStatementBuilder
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal class SqlSetOperationFlowQueryRunner<T>(
    private val context: SqlSetOperationContext<T>,
    private val options: SqlSetOperationOptions,
    private val transform: (R2dbcDialect, Row) -> T
) : R2dbcFlowQueryRunner<T> {

    override fun run(config: R2dbcDatabaseConfig): Flow<T> {
        if (!options.allowEmptyWhereClause) {
            checkWhereClauses(context.left)
            checkWhereClauses(context.right)
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        return executor.executeQuery(statement, transform)
    }

    private fun checkWhereClauses(subqueryContext: SubqueryContext<*>) {
        when (subqueryContext) {
            is SubqueryContext.EntitySelect -> {
                if (subqueryContext.context.where.isEmpty()) {
                    error("Empty where clause is not allowed.")
                }
            }
            is SubqueryContext.SqlSelect -> {
                if (subqueryContext.context.where.isEmpty()) {
                    error("Empty where clause is not allowed.")
                }
            }
            is SubqueryContext.SqlSetOperation -> {
                checkWhereClauses(subqueryContext.context.left)
                checkWhereClauses(subqueryContext.context.right)
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val aliasManager = DefaultAliasManager(context)
        val builder = SqlSetOperationStatementBuilder(config.dialect, context, aliasManager)
        return builder.build()
    }
}
