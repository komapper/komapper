package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityUpsertMultipleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityUpsertContext<ENTITY, ID, META>,
    option: InsertOption,
    private val entities: List<ENTITY>
) : R2dbcQueryRunner<Int> {

    private val support: EntityUpsertQueryRunnerSupport<ENTITY, ID, META> = EntityUpsertQueryRunnerSupport(context, option)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private suspend fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private suspend fun upsert(config: R2dbcDatabaseConfig, entities: List<ENTITY>): Int {
        val statement = buildStatement(config, entities)
        val (count) = support.upsert(config) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        if (entities.isEmpty()) return ""
        val statement = buildStatement(config, entities)
        return statement.toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig, entities: List<ENTITY>): Statement {
        return support.buildStatement(config, entities)
    }
}
