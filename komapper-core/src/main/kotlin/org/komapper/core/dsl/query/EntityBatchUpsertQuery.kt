package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.PropertyMetamodel

interface EntityBatchUpsertQuery<ENTITY : Any> : Query<Pair<IntArray, LongArray>> {
    fun set(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Pair<IntArray, LongArray>>
}

internal data class EntityBatchUpsertQueryImpl<ENTITY : Any>(
    private val context: EntityUpsertContext<ENTITY>,
    private val entities: List<ENTITY>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY>
) : EntityBatchUpsertQuery<ENTITY> {

    private val support: EntityUpsertQuerySupport<ENTITY> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Pair<IntArray, LongArray>> {
        val newContext = support.set(propertyMetamodels.toList())
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): Pair<IntArray, LongArray> {
        val newEntities = entities.map { preUpsert(config, it) }
        val statements = buildStatement(config, newEntities)
        val (count, keys) = upsert(config, statements)
        return count to keys
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
        return support.upsert(config) { it.executeBatch(statements) }
    }

    override fun dryRun(config: DatabaseConfig): String {
        return support.buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): List<Statement> {
        return entities.map { support.buildStatement(config, it) }
    }
}
