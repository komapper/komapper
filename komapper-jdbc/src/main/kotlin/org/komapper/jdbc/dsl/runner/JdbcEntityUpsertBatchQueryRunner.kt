package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.runner.EntityUpsertBatchQueryRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertBatchQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entities: List<ENTITY>
) : JdbcQueryRunner<List<Int>> {

    private val runner: EntityUpsertBatchQueryRunner<ENTITY, ID, META> =
        EntityUpsertBatchQueryRunner(context, options, entities)

    private val support: JdbcEntityUpsertQueryRunnerSupport<ENTITY, ID, META> = JdbcEntityUpsertQueryRunnerSupport(
        context,
        options
    )

    override fun run(config: JdbcDatabaseConfig): List<Int> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = entities.map { preUpsert(config, it) }
        val (counts) = upsert(config, newEntities)
        return counts.toList()
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entities: List<ENTITY>): Pair<IntArray, LongArray> {
        val statements = entities.map { runner.buildStatement(config, it) }
        return support.upsert(config) { it.executeBatch(statements) }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
