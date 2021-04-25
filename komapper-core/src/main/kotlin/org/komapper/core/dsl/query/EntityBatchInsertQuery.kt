package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityBatchInsertOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityBatchInsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> : Query<List<ID>> {
    fun option(configurator: QueryOptionConfigurator<EntityBatchInsertOption>): EntityBatchInsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): EntityBatchUpsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): Query<List<Int>>
}

internal data class EntityBatchInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchInsertOption = EntityBatchInsertOption()
) :
    EntityBatchInsertQuery<ENTITY, ID, META> {

    private val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityBatchInsertOption>): EntityBatchInsertQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator.apply(option))
    }

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *>): EntityBatchUpsertQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.UPDATE)
        return EntityBatchUpsertQueryImpl(newContext, entities, support)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *>): Query<List<Int>> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.IGNORE)
        return EntityBatchUpsertQueryImpl(newContext, entities, support)
    }

    override fun run(holder: DatabaseConfigHolder): List<ID> {
        if (entities.isEmpty()) return emptyList()
        val config = holder.config
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: DatabaseConfig, entities: List<ENTITY>): LongArray {
        val statements = entities.map { buildStatement(config, it) }
        val (_, keys) = support.insert(config) { it.executeBatch(statements) }
        return keys
    }

    private fun postInsert(entities: List<ENTITY>, generatedKeys: LongArray): List<ID> {
        val iterator = generatedKeys.iterator()
        return entities.asSequence().map {
            if (iterator.hasNext()) {
                support.postInsert(it, iterator.nextLong())
            } else {
                it
            }
        }.map {
            context.target.getId(it)
        }.toList()
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        if (entities.isEmpty()) return ""
        val config = holder.config
        val statement = buildStatement(config, entities.first())
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(config.dialect, context, entity)
        return builder.build()
    }
}
