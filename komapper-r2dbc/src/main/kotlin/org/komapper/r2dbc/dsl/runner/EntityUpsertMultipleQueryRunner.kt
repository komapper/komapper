package org.komapper.r2dbc.dsl.runner

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal data class EntityUpsertMultipleQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val option: InsertOption,
    private val entities: List<ENTITY>
) : R2dbcQueryRunner<Int> {

    private val support: EntityUpsertQuerySupport<ENTITY, ID, META> = EntityUpsertQuerySupport(context, option)

    override suspend fun run(config: R2dbcDatabaseConfig): Int {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: R2dbcDatabaseConfig, entity: ENTITY): ENTITY {
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
