package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlDeleteStatementBuilder
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlDeleteOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SqlDeleteQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlDeleteContext<ENTITY, ID, META>,
    private val options: SqlDeleteOptions
) : R2dbcQueryRunner<Int> {

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = SqlDeleteStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
