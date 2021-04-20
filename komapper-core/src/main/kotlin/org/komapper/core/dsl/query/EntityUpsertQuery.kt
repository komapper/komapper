package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

interface EntityUpsertQuery<ENTITY : Any, META : EntityMetamodel<ENTITY, META>> : Query<Pair<Int, Long?>> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Pair<Int, Long?>>
}

internal data class EntityUpsertQueryImpl<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    private val context: EntityUpsertContext<ENTITY, META>,
    private val entity: ENTITY,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, META>
) : EntityUpsertQuery<ENTITY, META> {

    private val support: EntityUpsertQuerySupport<ENTITY, META> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Pair<Int, Long?>> {
        val newContext = support.set(declaration)
        return copy(context = newContext)
    }

    override fun run(config: DatabaseConfig): Pair<Int, Long?> {
        val newEntity = preUpsert(config, entity)
        val statement = buildStatement(config, newEntity)
        val (count, keys) = upsert(config, statement)
        return count to keys.firstOrNull()
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, statement: Statement): Pair<Int, LongArray> {
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    override fun dryRun(config: DatabaseConfig): String {
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entity)
        return builder.build()
    }
}
