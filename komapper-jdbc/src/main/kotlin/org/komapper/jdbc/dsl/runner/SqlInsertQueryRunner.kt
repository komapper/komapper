package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.builder.SqlInsertStatementBuilder
import org.komapper.core.dsl.context.SqlInsertContext
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SqlInsertOptions
import org.komapper.jdbc.DatabaseConfig

internal class SqlInsertQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlInsertContext<ENTITY, ID, META>,
    private val options: SqlInsertOptions
) : JdbcQueryRunner<Pair<Int, Long?>> {

    override fun run(config: DatabaseConfig): Pair<Int, Long?> {
        val statement = buildStatement(config)
        val requiresGeneratedKeys = context.target.idAssignment() is Assignment.AutoIncrement<ENTITY, *, *>
        val executor = JdbcExecutor(config, options, requiresGeneratedKeys)
        val (count, keys) = executor.executeUpdate(statement)
        return count to keys.firstOrNull()
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlInsertStatementBuilder(config.dialect, context)
        return builder.build()
    }
}
