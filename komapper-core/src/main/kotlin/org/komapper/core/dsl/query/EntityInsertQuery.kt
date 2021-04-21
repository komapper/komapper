package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityInsertStatementBuilder
import org.komapper.core.dsl.context.DuplicateKeyType
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.core.dsl.option.QueryOptionConfigurator

interface EntityInsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> : Query<ID> {
    fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityInsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyUpdate(): EntityUpsertQuery<ENTITY, ID, META>
    fun onDuplicateKeyIgnore(): Query<Int>
}

internal data class EntityInsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val option: EntityInsertOption = EntityInsertOption()
) :
    EntityInsertQuery<ENTITY, ID, META> {

    private val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context, option)

    override fun option(configurator: QueryOptionConfigurator<EntityInsertOption>): EntityInsertQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator.apply(option))
    }

    override fun onDuplicateKeyUpdate(): EntityUpsertQuery<ENTITY, ID, META> {
        val newContext = context.asEntityUpsertContext(DuplicateKeyType.UPDATE)
        return EntityUpsertQueryImpl(newContext, entity, support)
    }

    override fun onDuplicateKeyIgnore(): Query<Int> {
        val newContext = context.asEntityUpsertContext(DuplicateKeyType.IGNORE)
        return EntityUpsertQueryImpl(newContext, entity, support)
    }

    override fun run(config: DatabaseConfig): ID {
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

    private fun postInsert(entity: ENTITY, generatedKeys: LongArray): ID {
        val key = generatedKeys.firstOrNull()
        val e = if (key == null) {
            entity
        } else {
            support.postInsert(entity, key)
        }
        return context.target.getId(e)
    }

    override fun dryRun(config: DatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityInsertStatementBuilder(config.dialect, context, entity)
        return builder.build()
    }
}
