package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityMultiInsertQuery<ENTITY : Any, META : EntityMetamodel<ENTITY, META>> : Query<List<ENTITY>> {
    fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityMultiInsertQuery<ENTITY, META>
    fun onDuplicateKeyUpdate(): EntityMultiUpsertQuery<ENTITY, META>
    fun onDuplicateKeyIgnore(): Query<Int>
}

internal data class EntityMultiInsertQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityInsertContext<ENTITY, META>,
    private val entities: List<ENTITY>,
    private val option: EntityInsertOption = EntityInsertOption()
) :
    EntityMultiInsertQuery<ENTITY, META> {

    private val support: EntityInsertQuerySupport<ENTITY, META> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityMultiInsertQueryImpl<ENTITY, META> {
        return copy(option = configurator.apply(option))
    }

    override fun onDuplicateKeyUpdate(): EntityMultiUpsertQuery<ENTITY, META> {
        val newContext = context.asEntityUpsertContext(DuplicateKeyType.UPDATE)
        return EntityMultiUpsertQueryImpl(newContext, entities, support)
    }

    override fun onDuplicateKeyIgnore(): Query<Int> {
        val newContext = context.asEntityUpsertContext(DuplicateKeyType.IGNORE)
        return EntityMultiUpsertQueryImpl(newContext, entities, support)
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
