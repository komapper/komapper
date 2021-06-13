package org.komapper.jdbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.jdbc.DatabaseConfig

internal class EntityUpsertBatchQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entities: List<ENTITY>
) : JdbcQueryRunner<List<Int>> {

    private val support: EntityUpsertQueryRunnerSupport<ENTITY, ID, META> = EntityUpsertQueryRunnerSupport(
        context,
        options
    )

    override fun run(config: DatabaseConfig): List<Int> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = entities.map { preUpsert(config, it) }
        val (counts) = upsert(config, newEntities)
        return counts.toList()
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, entities: List<ENTITY>): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
        return support.upsert(config) { it.executeBatch(statements) }
    }

    override fun dryRun(config: DatabaseConfig): String {
        if (entities.isEmpty()) return ""
        val statement = buildStatement(config, entities.first())
        return statement.toSql()
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }
}
