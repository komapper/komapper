package org.komapper.jdbc.dsl.query

import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.InsertOption
import org.komapper.jdbc.DatabaseConfig

internal data class EntityUpsertMultipleQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val option: InsertOption,
    private val entities: List<ENTITY>
) : Query<Int> {

    private val support: EntityUpsertQuerySupport<ENTITY, ID, META> = EntityUpsertQuerySupport(context, option)

    override fun run(config: DatabaseConfig): Int {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, entities: List<ENTITY>): Int {
        val statement = buildStatement(config, entities)
        val (count) = support.upsert(config) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: DatabaseConfig): String {
        if (entities.isEmpty()) return ""
        val statement = buildStatement(config, entities)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        return support.buildStatement(config, entities)
    }
}
