package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.scope.EntityBatchUpdateOptionDeclaration
import org.komapper.core.dsl.scope.EntityBatchUpdateOptionScope

interface EntityBatchUpdateQuery<ENTITY> : Query<List<ENTITY>> {
    fun option(declaration: EntityBatchUpdateOptionDeclaration): EntityBatchUpdateQuery<ENTITY>
}

internal data class EntityBatchUpdateQueryImpl<ENTITY>(
    private val context: EntityUpdateContext<ENTITY>,
    private val entities: List<ENTITY>,
    private val option: EntityBatchUpdateOption = QueryOptionImpl()
) :
    EntityBatchUpdateQuery<ENTITY> {

    private val support: EntityUpdateQuerySupport<ENTITY> = EntityUpdateQuerySupport(context, option)

    override fun option(declaration: EntityBatchUpdateOptionDeclaration): EntityBatchUpdateQueryImpl<ENTITY> {
        val scope = EntityBatchUpdateOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val newEntities = preUpdate(config)
        val statements = newEntities.map { buildStatement(config.dialect, it) }
        val (counts) = update(config, statements)
        return postUpdate(newEntities, counts)
    }

    private fun preUpdate(config: DatabaseConfig): List<ENTITY> {
        return entities.map { support.preUpdate(config, it) }
    }

    private fun update(config: DatabaseConfig, statements: List<Statement>): Pair<IntArray, LongArray> {
        return support.update(config) { it.executeBatch(statements) }
    }

    private fun postUpdate(entities: List<ENTITY>, counts: IntArray): List<ENTITY> {
        val iterator = counts.iterator()
        return entities.mapIndexed { index, entity ->
            val count = if (iterator.hasNext()) {
                iterator.nextInt()
            } else {
                error("Count value is not found. index=$index")
            }
            support.postUpdate(entity, count, index)
        }
    }

    override fun dryRun(dialect: Dialect): Statement {
        return buildStatement(dialect, entities.first())
    }

    private fun buildStatement(dialect: Dialect, entity: ENTITY): Statement {
        return support.buildStatement(dialect, entity)
    }
}
