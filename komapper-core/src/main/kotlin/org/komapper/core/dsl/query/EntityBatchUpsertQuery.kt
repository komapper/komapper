package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

interface EntityBatchUpsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> :
    Query<IntArray> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<IntArray>
}

internal data class EntityBatchUpsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, ID, META>
) : EntityBatchUpsertQuery<ENTITY, ID, META> {

    private val support: EntityUpsertQuerySupport<ENTITY, ID, META> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<IntArray> {
        val newContext = support.set(declaration)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): IntArray {
        if (entities.isEmpty()) return IntArray(0)
        val newEntities = entities.map { preUpsert(config, it) }
        val (counts) = upsert(config, newEntities)
        return counts
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
