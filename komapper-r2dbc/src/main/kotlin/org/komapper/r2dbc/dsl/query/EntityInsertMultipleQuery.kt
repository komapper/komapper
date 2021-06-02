package org.komapper.r2dbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.EntityInsertOption
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal data class EntityInsertMultipleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityInsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
    private val option: EntityInsertOption
) :
    Query<List<ENTITY>> {

    private val support: EntityInsertQuerySupport<ENTITY, ID, META> = EntityInsertQuerySupport(context, option)

    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: R2dbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): LongArray {
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

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        if (entities.isEmpty()) return ""
        val statement = buildStatement(config, entities)
        return statement.toString()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig, entities: List<ENTITY>): Statement {
        return support.buildStatement(config, entities)
    }
}
