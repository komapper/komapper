package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.options.SqlSetOperationOptions
import org.komapper.core.dsl.runner.SqlSetOperationQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal class JdbcSqlSetOperationQueryRunner<T : Any?, R>(
    private val context: SqlSetOperationContext<T>,
    private val options: SqlSetOperationOptions,
    private val transform: (JdbcDialect, ResultSet) -> T,
    private val collect: suspend (Flow<T>) -> R
) : JdbcQueryRunner<R> {

    private val runner: SqlSetOperationQueryRunner = SqlSetOperationQueryRunner(context, options)

    override fun run(config: JdbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause) {
            checkWhereClauses(context.left)
            checkWhereClauses(context.right)
        }
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        return executor.executeQuery(statement, transform, collect)
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
        return runner.dryRun(config)
    }
}
