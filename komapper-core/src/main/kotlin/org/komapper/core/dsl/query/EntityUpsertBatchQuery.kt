package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.QueryOption

internal data class EntityUpsertBatchQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: QueryOption
) : Query<List<Int>> {

    private val support: EntityUpsertQuerySupport<ENTITY, ID, META> = EntityUpsertQuerySupport(context, option)

    override fun run(holder: DatabaseConfigHolder): List<Int> {
        if (entities.isEmpty()) return emptyList()
        val config = holder.config
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

    override fun dryRun(holder: DatabaseConfigHolder): String {
        if (entities.isEmpty()) return ""
        val config = holder.config
        val statement = buildStatement(config, entities.first())
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }
}
