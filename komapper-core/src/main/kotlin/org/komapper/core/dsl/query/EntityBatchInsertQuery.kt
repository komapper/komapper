package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.scope.EntityBatchInsertOptionDeclaration
import org.komapper.core.dsl.scope.EntityBatchInsertOptionScope

interface EntityBatchInsertQuery<ENTITY : Any> : Query<List<ENTITY>> {
    fun option(declaration: EntityBatchInsertOptionDeclaration): EntityBatchInsertQuery<ENTITY>
}

internal data class EntityBatchInsertQueryImpl<ENTITY : Any>(
    private val context: EntityInsertContext<ENTITY>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchInsertOption = QueryOptionImpl()
) :
    EntityBatchInsertQuery<ENTITY> {

    private val support: EntityInsertQuerySupport<ENTITY> = EntityInsertQuerySupport(context, option)

    override fun option(declaration: EntityBatchInsertOptionDeclaration): EntityBatchInsertQueryImpl<ENTITY> {
        val scope = EntityBatchInsertOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val newEntities = preInsert(config)
        val statements = newEntities.map { buildStatement(config, it) }
        val (_, generatedKeys) = insert(config, statements)
        return postInsert(newEntities, generatedKeys)
    }

    private fun preInsert(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private fun insert(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
        return support.insert(config) { it.executeBatch(statements) }
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

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entities.first())
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
