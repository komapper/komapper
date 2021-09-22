package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityInsertOptions
import org.komapper.core.dsl.runner.EntityInsertSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityInsertSingleR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityInsertContext<ENTITY, ID, META>,
    options: EntityInsertOptions,
    private val entity: ENTITY
) : R2dbcRunner<ENTITY> {

    private val runner: EntityInsertSingleRunner<ENTITY, ID, META> =
        EntityInsertSingleRunner(context, options, entity)

    private val support: EntityInsertR2dbcRunnerSupport<ENTITY, ID, META> = EntityInsertR2dbcRunnerSupport(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY {
        val newEntity = preInsert(config)
        val (_, generatedKeys) = insert(config, newEntity)
        return postInsert(newEntity, generatedKeys)
    }

    private suspend fun preInsert(config: R2dbcDatabaseConfig): ENTITY {
        return support.preInsert(config, entity)
    }

    private suspend fun insert(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
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

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
