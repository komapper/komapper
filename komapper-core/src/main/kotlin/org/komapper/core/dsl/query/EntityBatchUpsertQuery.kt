package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

interface EntityBatchUpsertQuery<ENTITY : Any, META : EntityMetamodel<ENTITY, META>> : Query<Pair<IntArray, LongArray>> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Pair<IntArray, LongArray>>
}

internal data class EntityBatchUpsertQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityUpsertContext<ENTITY, META>,
    private val entities: List<ENTITY>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, META>
) : EntityBatchUpsertQuery<ENTITY, META> {

    private val support: EntityUpsertQuerySupport<ENTITY, META> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Pair<IntArray, LongArray>> {
        val newContext = support.set(declaration)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): Pair<IntArray, LongArray> {
        if (entities.isEmpty()) return IntArray(0) to LongArray(0)
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
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
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entity)
        return builder.build()
    }
}
