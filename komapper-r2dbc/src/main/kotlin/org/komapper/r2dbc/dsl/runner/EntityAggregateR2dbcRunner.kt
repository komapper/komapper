package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.query.EntityAggregate
import org.komapper.core.dsl.runner.EntityAggregateFactory
import org.komapper.core.dsl.runner.EntityKey
import org.komapper.core.dsl.runner.EntitySelectRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcExecutor

internal class EntityAggregateR2dbcRunner(
    private val context: EntitySelectContext<*, *, *>,
    private val options: EntitySelectOptions,
) : R2dbcRunner<EntityAggregate> {

    private val runner: EntitySelectRunner = EntitySelectRunner(context, options)

    private val aggregateFactory: EntityAggregateFactory = EntityAggregateFactory(context)

    override suspend fun run(config: R2dbcDatabaseConfig): EntityAggregate {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val rows: Flow<Map<EntityKey, Any>> = executor.executeQuery(statement) { dialect, r2dbcRow ->
            val row = mutableMapOf<EntityKey, Any>()
            val mapper = R2dbcEntityMapper(dialect, r2dbcRow)
            for (metamodel in context.projection.metamodels) {
                val entity = mapper.execute(metamodel) ?: continue
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val id = metamodel.getId(entity)
                val key = EntityKey(metamodel, id)
                row[key] = entity
            }
            row
        }
        return aggregateFactory.create(rows.toList())
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
