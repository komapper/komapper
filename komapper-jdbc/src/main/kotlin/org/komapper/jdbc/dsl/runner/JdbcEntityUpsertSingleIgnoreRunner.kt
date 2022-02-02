package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertSingleRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertSingleIgnoreRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
) : JdbcRunner<ENTITY?> {

    private val runner: EntityUpsertSingleRunner<ENTITY, ID, META> =
        EntityUpsertSingleRunner(context, entity)

    private val support: JdbcEntityUpsertRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertRunnerSupport(context)

    override fun run(config: JdbcDatabaseConfig): ENTITY? {
        val newEntity = preUpsert(config, entity)
        val (count, keys) = upsert(config, newEntity)
        return if (count == 0) null else postUpsert(newEntity, keys)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    private fun postUpsert(entity: ENTITY, generatedKeys: LongArray): ENTITY {
        val key = generatedKeys.firstOrNull()
        return if (key != null) {
            support.postUpsert(entity, key)
        } else {
            entity
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
