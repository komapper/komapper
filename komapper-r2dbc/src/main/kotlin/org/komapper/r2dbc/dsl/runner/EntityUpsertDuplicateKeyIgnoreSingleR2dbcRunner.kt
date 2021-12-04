package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityUpsertDuplicateKeyIgnoreSingleR2dbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : R2dbcRunner<ENTITY?> {

    private val runner: EntityUpsertSingleRunner<ENTITY, ID, META> =
        EntityUpsertSingleRunner(context, entity)

    private val support: EntityUpsertR2dbcRunnerSupport<ENTITY, ID, META> =
        EntityUpsertR2dbcRunnerSupport(context)

    override suspend fun run(config: R2dbcDatabaseConfig): ENTITY? {
        val newEntity = preUpsert(config, entity)
        val (count, keys) = upsert(config, newEntity)
        return if (count == 0) null else postUpsert(newEntity, keys)
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    private fun postUpsert(entity: ENTITY, generatedKeys: LongArray): ENTITY {
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
