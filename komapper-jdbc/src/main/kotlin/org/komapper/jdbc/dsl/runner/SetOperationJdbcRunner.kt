package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SetOperationContext
import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.runner.SetOperationRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal class SetOperationJdbcRunner<T : Any?, R>(
    private val context: SetOperationContext,
    private val options: SelectOptions,
    private val transform: (JdbcDialect, ResultSet) -> T,
    private val collect: suspend (Flow<T>) -> R
) : JdbcRunner<R> {

    private val runner: SetOperationRunner = SetOperationRunner(context, options)

    override fun run(config: JdbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause) {
            checkWhereClauses(context.left)
            checkWhereClauses(context.right)
        }
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        return executor.executeQuery(statement, transform, collect)
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
