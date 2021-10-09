package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.core.dsl.runner.SqlInsertRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class SqlInsertJdbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val options: SqlInsertOptions
) : JdbcRunner<Pair<Int, ID?>> {

    private val runner: SqlInsertRunner<ENTITY, ID, META> = SqlInsertRunner(context, options)

    override fun run(config: JdbcDatabaseConfig): Pair<Int, ID?> {
        val statement = runner.buildStatement(config)
        val requiresGeneratedKeys = context.target.idAssignment() is Assignment.AutoIncrement<ENTITY, *, *>
        val executor = JdbcExecutor(config, options, requiresGeneratedKeys)
        val (count, keys) = executor.executeUpdate(statement)
        val id = keys.firstOrNull()?.let { context.target.toId(it) }
        return count to id
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
