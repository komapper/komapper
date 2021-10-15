package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdAssignment
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.runner.SqlInsertRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class SqlInsertR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val options: SqlInsertOptions
) : R2dbcRunner<Pair<Int, ID?>> {

    private val runner: SqlInsertRunner<ENTITY, ID, META> = SqlInsertRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): Pair<Int, ID?> {
        val statement = runner.buildStatement(config)
        val generatedColumn = when (val assignment = context.target.idAssignment()) {
            is IdAssignment.AutoIncrement<ENTITY, *> -> assignment.property.columnName
            else -> null
        }
        val executor = R2dbcExecutor(config, options, generatedColumn)
        val (count, keys) = executor.executeUpdate(statement)
        val id = keys.firstOrNull()?.let { context.target.toId(it) }
        return count to id
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
