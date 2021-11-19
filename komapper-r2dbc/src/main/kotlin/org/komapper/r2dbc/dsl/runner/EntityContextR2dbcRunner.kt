package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.query.EntityContext
import org.komapper.core.dsl.runner.EntityContextFactory
import org.komapper.core.dsl.runner.SelectRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class EntityContextR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
    private val options: SelectOptions,
) : R2dbcRunner<EntityContext<ENTITY>> {

    private val runner: SelectRunner = SelectRunner(context, options)
    private val factory: EntityContextFactory<ENTITY, ID, META> = EntityContextFactory(context)

    override suspend fun run(config: R2dbcDatabaseConfig): EntityContext<ENTITY> {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val rows: Flow<Map<EntityMetamodel<*, *, *>, Any>> = executor.executeQuery(statement) { dialect, r2dbcRow ->
            val row = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
            val mapper = R2dbcEntityMapper(dialect, r2dbcRow)
            for (metamodel in context.projection.metamodels()) {
                val entity = mapper.execute(metamodel) ?: continue
                row[metamodel] = entity
            }
            row
        }
        return factory.create(rows.toList())
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
