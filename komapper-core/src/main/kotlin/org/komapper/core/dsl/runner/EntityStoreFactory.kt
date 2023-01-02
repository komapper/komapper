package org.komapper.core.dsl.runner

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.query.EntityStoreImpl

@ThreadSafe
class EntityStoreFactory {

    fun create(
        metamodels: Set<EntityMetamodel<*, *, *>>,
        rows: List<Map<EntityMetamodel<*, *, *>, Any>>,
    ): EntityStore {
        val entitySets: Map<EntityMetamodel<*, *, *>, MutableSet<Any>> =
            metamodels.associateWith { mutableSetOf() }
        val cache: MutableMap<EntityKey, Any> = mutableMapOf()
        val newRows = ArrayList<Map<EntityMetamodel<*, *, *>, Any>>(rows.size)
        for (row in rows) {
            val newRow = LinkedHashMap<EntityMetamodel<*, *, *>, Any>(row.size)
            for ((metamodel, entity) in row) {
                entitySets[metamodel].let {
                    checkNotNull(it) { "metamodel not found: $metamodel" }
                    it.add(entity)
                }
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val id = metamodel.extractId(entity)
                val key = EntityKey(metamodel, id)
                val prev = cache.putIfAbsent(key, entity)
                newRow[metamodel] = prev ?: entity
            }
            newRows.add(newRow)
        }
        return EntityStoreImpl(entitySets, newRows)
    }
}
