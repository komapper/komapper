package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.options.SqlSelectOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal class SqlSelectFlowQueryRunner<T>(
    private val context: SqlSelectContext<*, *, *>,
    private val options: SqlSelectOptions,
    private val transform: (R2dbcDialect, Row) -> T,
) :
    R2dbcFlowQueryRunner<T> {

    override fun run(config: R2dbcDatabaseConfig): Flow<T> {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        return executor.executeQuery(statement, transform)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
