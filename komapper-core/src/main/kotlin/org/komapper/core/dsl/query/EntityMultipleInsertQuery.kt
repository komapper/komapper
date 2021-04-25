package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityInsertOption

interface EntityMultipleInsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> : Query<List<ENTITY>> {
    fun option(configurator: (EntityInsertOption) -> EntityInsertOption): EntityMultipleInsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): EntityMultipleUpsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): Query<Int>
}

internal data class EntityMultipleInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityInsertOption = EntityInsertOption()
) :
    EntityMultipleInsertQuery<ENTITY, ID, META> {

    private val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: (EntityInsertOption) -> EntityInsertOption): EntityMultipleInsertQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator(option))
    }

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *>): EntityMultipleUpsertQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.UPDATE)
        return EntityMultipleUpsertQueryImpl(newContext, entities, support)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *>): Query<Int> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.IGNORE)
        return EntityMultipleUpsertQueryImpl(newContext, entities, support)
    }

    override fun run(holder: DatabaseConfigHolder): List<ENTITY> {
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
        val statement = buildStatement(config, entities)
        val (_, keys) = support.insert(config) { it.executeUpdate(statement) }
        return keys
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

    override fun dryRun(holder: DatabaseConfigHolder): String {
        if (entities.isEmpty()) return ""
        val config = holder.config
        val statement = buildStatement(config, entities)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityMultipleInsertStatementBuilder(context, entities)
        return builder.build()
    }
}
