package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.SqlInsertOption
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal data class SqlInsertQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val option: SqlInsertOption = SqlInsertOption.default
) : R2dbcQueryRunner<Pair<Int, Long?>> {

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Int, Long?> {
        val statement = buildStatement(config)
        val generatedColumn = when (context.target.idAssignment()) {
            // TODO
            is Assignment.AutoIncrement<ENTITY, *, *> -> context.target.idProperties().first().columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, option, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        return count to keys.firstOrNull()
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = SqlInsertStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
