package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.DefaultAliasManager
import org.komapper.core.dsl.builder.SqlSetOperationStatementBuilder
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.option.SqlSetOperationOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal data class SqlSetOperationQueryRunner<T, R>(
    private val context: SqlSetOperationContext<T>,
    private val option: SqlSetOperationOption,
    private val provide: (R2dbcDialect, Row) -> T,
    private val collect: suspend (Flow<T>) -> R
) : R2dbcQueryRunner<R> {

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        if (!option.allowEmptyWhereClause) {
            checkWhereClauses(context.left)
            checkWhereClauses(context.right)
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(statement, provide, collect)
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

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val aliasManager = DefaultAliasManager(context)
        val builder = SqlSetOperationStatementBuilder(config.dialect, context, aliasManager)
        return builder.build()
    }
}
