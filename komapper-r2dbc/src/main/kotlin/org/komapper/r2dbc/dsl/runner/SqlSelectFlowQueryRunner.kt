package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlSelectOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal data class SqlSelectFlowQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlSelectContext<ENTITY, ID, META>,
    private val option: SqlSelectOption,
    private val transform: (R2dbcDialect, Row) -> ENTITY,
) :
    R2dbcFlowQueryRunner<ENTITY> {

    override fun run(config: R2dbcDatabaseConfig): Flow<ENTITY> {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, option)
        return executor.executeQuery(statement, transform)
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = SqlSelectStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
