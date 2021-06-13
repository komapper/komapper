package org.komapper.r2dbc.dsl.runner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.EntitySelectStatementBuilder
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntitySelectOptions
import org.komapper.r2dbc.R2dbcDatabaseConfig
import kotlin.reflect.cast

internal class EntitySelectQueryRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>, R>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
    private val options: EntitySelectOptions,
    private val collect: suspend (Flow<ENTITY>) -> R
) : R2dbcQueryRunner<R> {

    override suspend fun run(config: R2dbcDatabaseConfig): R {
        if (!options.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = R2dbcExecutor(config, options)
        val rows: Flow<Map<EntityKey, Any>> = executor.executeQuery(statement) { dialect, r2dbcRow ->
            val row = mutableMapOf<EntityKey, Any>()
            val mapper = EntityMapper(dialect, r2dbcRow)
            for (metamodel in context.projection.metamodels) {
                val entity = mapper.execute(metamodel) ?: continue
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val id = metamodel.getId(entity)
                val key = EntityKey(metamodel, id)
                row[key] = entity
            }
            row
        }
        val pool: MutableMap<EntityKey, Any> = mutableMapOf()
        for (row in rows.toList()) {
            val entityKeys: MutableMap<EntityMetamodel<*, *, *>, EntityKey> = mutableMapOf()
            for ((key, entity) in row) {
                pool.putIfAbsent(key, entity)
                entityKeys[key.entityMetamodel] = key
            }
            associate(entityKeys, pool)
        }
        return pool.entries.asFlow().filter {
            it.key.entityMetamodel == context.target
        }.map {
            context.target.klass().cast(it.value)
        }.let {
            collect(it)
        }
    }

    override fun dryRun(config: R2dbcDatabaseConfig): String {
        return buildStatement(config).toSql()
    }

    private fun buildStatement(config: R2dbcDatabaseConfig): Statement {
        val builder = EntitySelectStatementBuilder(config.dialect, context)
        return builder.build()
    }

    private fun associate(
        entityKeys: Map<EntityMetamodel<*, *, *>, EntityKey>,
        pool: MutableMap<EntityKey, Any>
    ) {
        for ((association, associator) in context.associatorMap) {
            val key1 = entityKeys[association.first]
            val key2 = entityKeys[association.second]
            if (key1 == null || key2 == null) {
                continue
            }
            val entity1 = pool[key1]!!
            val entity2 = pool[key2]!!
            val newEntity = associator.apply(entity1, entity2)
            pool.replace(key1, newEntity)
        }
    }
}

private data class EntityKey(
    val entityMetamodel: EntityMetamodel<*, *, *>,
    val id: Any
)
