package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.runner.EntityUpdateSingleRunner
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityUpdateSingleJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpdateContext<ENTITY, ID, META>,
    options: UpdateOptions,
    private val entity: ENTITY
) : JdbcRunner<ENTITY> {

    private val runner: EntityUpdateSingleRunner<ENTITY, ID, META> =
        EntityUpdateSingleRunner(context, options, entity)

    private val support: EntityUpdateJdbcyRunnerSupport<ENTITY, ID, META> =
        EntityUpdateJdbcyRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): ENTITY {
        val newEntity = preUpdate(config, entity)
        val (count) = update(config, newEntity)
        return postUpdate(newEntity, count)
    }

    private fun preUpdate(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpdate(config, entity)
    }

    private fun update(config: JdbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config, entity)
        return support.update(config) { it.executeUpdate(statement) }
    }

    private fun postUpdate(entity: ENTITY, count: Int): ENTITY {
        return support.postUpdate(entity, count)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}
