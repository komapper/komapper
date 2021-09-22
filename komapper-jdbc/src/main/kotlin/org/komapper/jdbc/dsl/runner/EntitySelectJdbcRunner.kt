package org.komapper.jdbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.core.dsl.runner.EntityAggregator
import org.komapper.core.dsl.runner.EntityKey
import org.komapper.core.dsl.runner.EntitySelectRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class EntitySelectJdbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: EntitySelectOptions,
    private val collect: suspend (Flow<ENTITY>) -> R
) : JdbcRunner<R> {

    private val runner: EntitySelectRunner<ENTITY, ID, META> =
        EntitySelectRunner(context, options)

    private val aggregator: EntityAggregator<ENTITY, ID, META> =
        EntityAggregator(context)

    override fun run(config: JdbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = runner.buildStatement(config)
        val executor = JdbcExecutor(config, options)
        val rows = executor.executeQuery(statement) { rs ->
            val rows = mutableListOf<Map<EntityKey, Any>>()
            while (rs.next()) {
                val row = mutableMapOf<EntityKey, Any>()
                val mapper = JdbcEntityMapper(config.dialect, rs)
                for (metamodel in context.projection.metamodels) {
                    val entity = mapper.execute(metamodel) ?: continue
                    @Suppress("UNCHECKED_CAST")
                    metamodel as EntityMetamodel<Any, Any, *>
                    val id = metamodel.getId(entity)
                    val key = EntityKey(metamodel, id)
                    row[key] = entity
                }
                rows.add(row)
            }
            rows
        }
        val entities = aggregator.aggregate(rows)
        return runBlocking { collect(entities) }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
