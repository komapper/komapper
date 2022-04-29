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

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): ENTITY? {
        val newEntity = preUpsert(config, entity)
        val (count, keys) = upsert(config, newEntity)
        return if (count == 0L) null else postUpsert(newEntity, keys)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entity: ENTITY): Pair<Long, List<Long>> {
        val statement = runner.buildStatement(config, entity)
        return support.upsert(config, true) { it.executeUpdate(statement) }
    }

    private fun postUpsert(entity: ENTITY, generatedKeys: List<Long>): ENTITY {
        return runner.postUpsert(entity, generatedKeys)
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
