package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.runner.EntityInsertMultipleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityInsertMultipleR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entities: List<ENTITY>
) :
    R2dbcRunner<List<ENTITY>> {

    private val runner: EntityInsertMultipleRunner<ENTITY, ID, META> =
        EntityInsertMultipleRunner(context, options, entities)

    private val support: EntityInsertR2dbcRunnerSupport<ENTITY, ID, META> = EntityInsertR2dbcRunnerSupport(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): List<ENTITY> {
        if (entities.isEmpty()) return emptyList()
        val newEntities = preInsert(config)
        val generatedKeys = insert(config, newEntities)
        return postInsert(newEntities, generatedKeys)
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): List<ENTITY> {
        return entities.map { support.preInsert(config, it) }
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): LongArray {
        val statement = runner.buildStatement(config, entities)
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

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
