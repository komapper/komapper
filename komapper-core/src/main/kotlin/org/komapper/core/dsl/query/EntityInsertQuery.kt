package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.EntityInsertOption

interface EntityInsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> : Query<ENTITY> {
    fun option(configurator: (EntityInsertOption) -> EntityInsertOption): EntityInsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): EntityUpsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *> = emptyArray()): Query<Int>
}

internal data class EntityInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val option: EntityInsertOption = EntityInsertOption()
) :
    EntityInsertQuery<ENTITY, ID, META> {

    private val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: (EntityInsertOption) -> EntityInsertOption): EntityInsertQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator(option))
    }

    override fun onDuplicateKeyUpdate(vararg keys: PropertyMetamodel<ENTITY, *>): EntityUpsertQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.UPDATE)
        return EntityUpsertQueryImpl(newContext, entity, support)
    }

    override fun onDuplicateKeyIgnore(vararg keys: PropertyMetamodel<ENTITY, *>): Query<Int> {
        val newContext = context.asEntityUpsertContext(keys.toList(), DuplicateKeyType.IGNORE)
        return EntityUpsertQueryImpl(newContext, entity, support)
    }

    override fun run(holder: DatabaseConfigHolder): ENTITY {
        val config = holder.config
        val newEntity = preInsert(config)
        val (_, generatedKeys) = insert(config, newEntity)
        return postInsert(newEntity, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private fun insert(config: DatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.insert(config) { it.executeUpdate(statement) }
    }

    private fun postInsert(entity: ENTITY, generatedKeys: LongArray): ENTITY {
        val key = generatedKeys.firstOrNull()
        return if (key != null) {
            support.postInsert(entity, key)
        } else {
            entity
        }
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(config.dialect, context, entity)
        return builder.build()
    }
}
