package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.option.EntityBatchInsertOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityBatchInsertQuery<ENTITY : Any> : Query<List<ENTITY>> {
    fun option(configurator: QueryOptionConfigurator<EntityBatchInsertOption>): EntityBatchInsertQuery<ENTITY>
    fun onDuplicateKeyUpdate(): EntityBatchUpsertQuery<ENTITY>
    fun onDuplicateKeyIgnore(): Query<Pair<IntArray, LongArray>>
}

internal data class EntityBatchInsertQueryImpl<ENTITY : Any>(
    private val context: EntityInsertContext<ENTITY>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchInsertOption = EntityBatchInsertOption()
) :
    EntityBatchInsertQuery<ENTITY> {

    private val support: EntityInsertQuerySupport<ENTITY> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityBatchInsertOption>): EntityBatchInsertQueryImpl<ENTITY> {
        return copy(option = configurator.apply(option))
    }

    override fun onDuplicateKeyUpdate(): EntityBatchUpsertQuery<ENTITY> {
        val newContext = context.asEntityUpsertContext(DuplicateKeyType.UPDATE)
        return EntityBatchUpsertQueryImpl(newContext, entities, option)
    }

    override fun onDuplicateKeyIgnore(): Query<Pair<IntArray, LongArray>> {
        val newContext = context.asEntityUpsertContext(DuplicateKeyType.IGNORE)
        return EntityBatchUpsertQueryImpl(newContext, entities, option)
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: DatabaseConfig, entities: List<ENTITY>): LongArray {
        val builder = config.dialect.getEntityMultiInsertStatementBuilder(context, entities)
        return if (builder != null) {
            val statement = builder.build()
            val (_, keys) = support.insert(config) { it.executeUpdate(statement) }
            keys
        } else {
            val statements = entities.map { buildStatement(config, it) }
            val (_, keys) = support.insert(config) { it.executeBatch(statements) }
            keys
        }
    }

    private fun postInsert(entities: List<ENTITY>, generatedKeys: LongArray): List<ENTITY> {
        val iterator = generatedKeys.iterator()
        return entities.map {
            if (iterator.hasNext()) {
                support.postInsert(it, iterator.nextLong())
            } else {
                it
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        val builder = config.dialect.getEntityMultiInsertStatementBuilder(context, entities)
        if (builder != null) {
            return builder.build().sql
        }
        return buildStatement(config, entities.first()).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
