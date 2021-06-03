package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal data class SqlSelectQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
    private val context: SqlSelectContext<ENTITY, ID, META>,
    private val option: SqlSelectOption,
    private val provide: (JdbcDialect, ResultSet) -> ENTITY,
    private val transform: (Sequence<ENTITY>) -> R
) :
    JdbcQueryRunner<R> {
    
    override fun run(config: DatabaseConfig): R {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option)
        return executor.executeQuery(statement, provide, transform)
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
