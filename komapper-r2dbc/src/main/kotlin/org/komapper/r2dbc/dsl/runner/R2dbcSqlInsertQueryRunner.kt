package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.runner.SqlInsertQueryRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class R2dbcSqlInsertQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val options: SqlInsertOptions = SqlInsertOptions.default
) : R2dbcQueryRunner<Pair<Int, Long?>> {

    private val runner: SqlInsertQueryRunner<ENTITY, ID, META> = SqlInsertQueryRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Int, Long?> {
        val statement = runner.buildStatement(config)
        val generatedColumn = when (val assignment = context.target.idAssignment()) {
            is Assignment.AutoIncrement<ENTITY, *, *> -> assignment.columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, options, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        return count to keys.firstOrNull()
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
