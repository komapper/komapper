package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.jdbc.JdbcDatabaseConfig

internal class EntityUpsertSingleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    options: InsertOptions,
    private val entity: ENTITY,
) : JdbcQueryRunner<Int> {

    private val support: EntityUpsertQueryRunnerSupport<ENTITY, ID, META> = EntityUpsertQueryRunnerSupport(context, options)

    override fun run(config: JdbcDatabaseConfig): Int {
        val newEntity = preUpsert(config, entity)
        val (count) = upsert(config, newEntity)
        return count
    }

    private fun preUpsert(config: JdbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: JdbcDatabaseConfig, entity: ENTITY): Pair<Int, LongArray> {
        val statement = buildStatement(config, entity)
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return buildStatement(config, entity)
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, listOf(entity))
    }
}
