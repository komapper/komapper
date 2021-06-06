package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

class SqlTripleColumnsQueryRunner<A : Any, B : Any, C : Any, R>(
    private val context: SqlSelectContext<*, *, *>,
    private val option: SqlSelectOption,
    val provide: (JdbcDialect, ResultSet) -> Triple<A?, B?, C?>,
    private val collect: suspend (Flow<Triple<A?, B?, C?>>) -> R
) : JdbcQueryRunner<R> {

    override fun run(config: DatabaseConfig): R {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        return executor.executeQuery(statement, provide, collect)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
