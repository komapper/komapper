package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.runner.EntitySelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.JdbcExecutor
import java.sql.ResultSet

internal class EntitySelectJdbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: EntitySelectOptions,
    private val transform: (JdbcDialect, ResultSet) -> ENTITY,
    private val collect: suspend (Flow<ENTITY>) -> R
) : JdbcRunner<R> {

    private val runner: EntitySelectRunner =
        EntitySelectRunner(context, options)

    override fun run(config: JdbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        return executor.executeQuery(statement, transform, collect)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
