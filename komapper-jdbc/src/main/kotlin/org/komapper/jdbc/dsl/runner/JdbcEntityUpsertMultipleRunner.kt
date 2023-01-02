package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityUpsertMultipleRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class JdbcEntityUpsertMultipleRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    private val entities: List<ENTITY>,
) : JdbcRunner<Long> {

    private val runner: EntityUpsertMultipleRunner<ENTITY, ID, META> =
        EntityUpsertMultipleRunner(context, entities)

    private val support: JdbcEntityUpsertRunnerSupport<ENTITY, ID, META> =
        JdbcEntityUpsertRunnerSupport(context)

    override fun check(config: DatabaseConfig) {
        runner.check(config)
    }

    override fun run(config: JdbcDatabaseConfig): Long {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entities: List<ENTITY>): Long {
        val statement = runner.buildStatement(config, entities)
        val (count) = support.upsert(config, false) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}
