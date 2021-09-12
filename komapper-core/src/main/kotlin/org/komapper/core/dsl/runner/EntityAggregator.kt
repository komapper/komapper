package org.komapper.core.dsl.runner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import kotlin.reflect.cast

class EntityAggregator<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>
) {

    fun aggregate(rows: List<Map<EntityKey, Any>>): Flow<ENTITY> {
        // hold only unique entities
        val pool: MutableMap<EntityKey, Any> = mutableMapOf()
        for (row in rows) {
            val entityKeys: MutableMap<EntityMetamodel<*, *, *>, EntityKey> = mutableMapOf()
            for ((key, entity) in row) {
                pool.putIfAbsent(key, entity)
                entityKeys[key.entityMetamodel] = key
            }
            associate(pool, entityKeys)
        }
        return pool.entries.asFlow().filter {
            it.key.entityMetamodel == context.target
        }.map {
            context.target.klass().cast(it.value)
        }
    }

    private fun associate(
        pool: MutableMap<EntityKey, Any>,
        entityKeys: Map<EntityMetamodel<*, *, *>, EntityKey>
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
