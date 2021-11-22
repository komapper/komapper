package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.runner.EntityUpdateBatchRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityUpdateBatchJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    options: UpdateOptions,
    private val entities: List<ENTITY>
) :
    JdbcRunner<List<ENTITY>> {

    private val runner: EntityUpdateBatchRunner<ENTITY, ID, META> =
        EntityUpdateBatchRunner(context, options, entities)

    private val support: EntityUpdateJdbcyRunnerSupport<ENTITY, ID, META> =
        EntityUpdateJdbcyRunnerSupport(context, options)

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
        val statements = entities.map { runner.buildStatement(config, it) }
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
        return runner.dryRun(config)
    }
}
