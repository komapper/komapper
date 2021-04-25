package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DatabaseConfigHolder
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.SetScope

interface EntityUpsertQuery<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> : Query<Int> {
    fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Int>
}

internal data class EntityUpsertQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntityUpsertContext<ENTITY, ID, META>,
    private val entity: ENTITY,
    private val insertSupport: EntityInsertQuerySupport<ENTITY, ID, META>
) : EntityUpsertQuery<ENTITY, ID, META> {

    private val support: EntityUpsertQuerySupport<ENTITY, ID, META> = EntityUpsertQuerySupport(context, insertSupport)

    override fun set(declaration: SetScope<ENTITY>.(META) -> Unit): Query<Int> {
        val newContext = support.set(declaration)
        return copy(context = newContext)
    }

    override fun run(holder: DatabaseConfigHolder): Int {
        val config = holder.config
        val newEntity = preUpsert(config, entity)
        val statement = buildStatement(config, newEntity)
        val (count) = upsert(config, statement)
        return count
    }

    private fun preUpsert(config: DatabaseConfig, entity: ENTITY): ENTITY {
        return support.preUpsert(config, entity)
    }

    private fun upsert(config: DatabaseConfig, statement: Statement): Pair<Int, LongArray> {
        return support.upsert(config) { it.executeUpdate(statement) }
    }

    override fun dryRun(holder: DatabaseConfigHolder): String {
        val config = holder.config
        val statement = buildStatement(config, entity)
        return statement.sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = config.dialect.getEntityUpsertStatementBuilder(context, entity)
        return builder.build()
    }
}
