package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal class SqlSelectQueryRunner<T, R>(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val transform: (JdbcDialect, ResultSet) -> T,
    private val collect: suspend (Flow<T>) -> R
) :
    JdbcQueryRunner<R> {

    override fun run(config: JdbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, options)
        return executor.executeQuery(statement, transform, collect)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
