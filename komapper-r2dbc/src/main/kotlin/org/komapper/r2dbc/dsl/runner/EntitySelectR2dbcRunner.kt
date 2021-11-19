package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.Flow
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.runner.EntitySelectRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDialect
import org.komapper.r2dbc.R2dbcExecutor

internal class EntitySelectR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: SelectOptions,
    private val transform: (R2dbcDialect, Row) -> ENTITY,
    private val collect: suspend (Flow<ENTITY>) -> R
) : R2dbcRunner<R> {

    private val runner: EntitySelectRunner = EntitySelectRunner(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val entities = executor.executeQuery(statement, transform)
        return collect(entities)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
