package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

interface EntityBatchUpsertQuery<ENTITY : Any, META : EntityMetamodel<ENTITY, META>> : Query<LongArray> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<LongArray>
}

internal data class EntityBatchUpsertQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityUpsertContext<ENTITY, META>,
    private val entities: List<ENTITY>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, META>
) : EntityBatchUpsertQuery<ENTITY, META> {

    private val support: EntityUpsertQuerySupport<ENTITY, META> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<LongArray> {
        val newContext = support.set(declaration)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): LongArray {
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, entities: List<ENTITY>): LongArray {
        val builder = config.dialect.getEntityMultiUpsertStatementBuilder(context, entities)
        return if (builder != null) {
            val statement = builder.build()
            val (_, keys) = support.upsert(config) { it.executeUpdate(statement) }
            keys
        } else {
            val statements = buildStatement(config, entities)
            val (_, keys) = support.upsert(config) { it.executeBatch(statements) }
            keys
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        val builder = config.dialect.getEntityMultiUpsertStatementBuilder(context, entities)
        return builder?.build()?.sql
            ?: support.buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): List<Statement> {
        return entities.map { support.buildStatement(config, it) }
    }
}
