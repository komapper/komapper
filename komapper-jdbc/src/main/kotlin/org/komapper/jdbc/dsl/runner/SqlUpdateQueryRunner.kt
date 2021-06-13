package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlUpdateOptions
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class SqlUpdateQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlUpdateContext<ENTITY, ID, META>,
    private val options: SqlUpdateOptions
) : JdbcQueryRunner<Int> {

    override fun run(config: JdbcDatabaseConfig): Int {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config)
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlUpdateStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
