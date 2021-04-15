package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.option.QueryOption
import org.komapper.core.dsl.scope.SetDeclaration

interface EntityUpsertQuery<ENTITY : Any> : Query<Pair<Int, Long?>> {
    fun set(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Pair<Int, Long?>>
    fun set(declaration: SetDeclaration<ENTITY>): Query<Pair<Int, Long?>>
}

internal data class EntityUpsertQueryImpl<ENTITY : Any>(
    private val context: EntityUpsertContext<ENTITY>,
    private val entity: ENTITY,
    private val option: QueryOption
) : EntityUpsertQuery<ENTITY> {

    private val support: EntityUpsertQuerySupport<ENTITY> = EntityUpsertQuerySupport(context, option)

    override fun set(vararg propertyMetamodels: PropertyMetamodel<ENTITY, *>): Query<Pair<Int, Long?>> {
        val newContext = support.set(propertyMetamodels.toList())
        return copy(context = newContext)
    }

    override fun set(declaration: SetDeclaration<ENTITY>): Query<Pair<Int, Long?>> {
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
        return buildStatement(config, entity).sql
    }

    private fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        return support.buildStatement(config, entity)
    }
}
