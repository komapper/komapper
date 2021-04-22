package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityMultiInsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> : Query<List<ID>> {
    fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityMultiInsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): EntityMultiUpsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): Query<Int>
}

internal data class EntityMultiInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityInsertOption = EntityInsertOption()
) :
    EntityMultiInsertQuery<ENTITY, ID, META> {

    private val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityMultiInsertQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator.apply(option))
    }

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *>): EntityMultiUpsertQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.UPDATE)
        return EntityMultiUpsertQueryImpl(newContext, entities, support)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *>): Query<Int> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.IGNORE)
        return EntityMultiUpsertQueryImpl(newContext, entities, support)
    }

    override fun run(config: DatabaseConfig): List<ID> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: DatabaseConfig, entities: List<ENTITY>): LongArray {
        val statement = buildStatement(config, entities)
        val (_, keys) = support.insert(config) { it.executeUpdate(statement) }
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

    override fun dryRun(config: DatabaseConfig): String {
        if (entities.isEmpty()) return ""
        val statement = buildStatement(config, entities)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityMultiInsertStatementBuilder(context, entities)
        return builder.build()
    }
}
