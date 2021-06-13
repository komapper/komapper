package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityUpdateBatchOptions
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityUpdateBatchQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    options: EntityUpdateBatchOptions,
    private val entities: List<ENTITY>
) :
    JdbcQueryRunner<List<ENTITY>> {

    private val support: EntityUpdateQueryRunnerSupport<ENTITY, ID, META> =
        EntityUpdateQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preUpdate(config)
        val (counts) = update(config, newEntities)
        return postUpdate(newEntities, counts)
    }

    private fun preUpdate(config: JdbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preUpdate(config, it) }
    }

    private fun update(config: JdbcDatabaseConfig, entities: List<ENTITY>): Pair<IntArray, LongArray> {
        val statements = entities.map { buildStatement(config, it) }
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

    override fun dryRun(config: DatabaseConfig): Statement {
        if (entities.isEmpty()) return Statement.EMPTY
        return buildStatement(config, entities.first())
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
