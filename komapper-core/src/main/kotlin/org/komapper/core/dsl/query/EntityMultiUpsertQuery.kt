package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

interface EntityMultiUpsertQuery<ENTITY : Any, META : EntityMetamodel<ENTITY, META>> : Query<Int> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Int>
}

internal data class EntityMultiUpsertQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityUpsertContext<ENTITY, META>,
    private val entities: List<ENTITY>,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, META>
) : EntityMultiUpsertQuery<ENTITY, META> {

    private val support: EntityUpsertQuerySupport<ENTITY, META> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Int> {
        val newContext = support.set(declaration)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): Int {
        if (entities.isEmpty()) return 0
        val newEntities = entities.map { preUpsert(config, it) }
        return upsert(config, newEntities)
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, entities: List<ENTITY>): Int {
        val builder = config.dialect.getEntityMultiUpsertStatementBuilder(context, entities)
        val statement = builder.build()
        val (count) = support.upsert(config) { it.executeUpdate(statement) }
        return count
    }

    override fun dryRun(config: DatabaseConfig): String {
        if (entities.isEmpty()) return ""
        val statement = buildStatement(config, entities)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entities: List<ENTITY>): Statement {
        val builder = config.dialect.getEntityMultiUpsertStatementBuilder(context, entities)
        return builder.build()
    }
}
