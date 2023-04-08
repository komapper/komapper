package org.komapper.core.dsl.runner

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityRef
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
        val cache: MutableMap<EntityRef<*, *, *>, EntityRef<*, *, *>> = mutableMapOf()
        val newRows = ArrayList<Map<EntityMetamodel<*, *, *>, EntityRef<*, *, *>>>(rows.size)
        for (row in rows) {
            val newRow = LinkedHashMap<EntityMetamodel<*, *, *>, EntityRef<*, *, *>>(row.size)
            for ((metamodel, entity) in row) {
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val ref = EntityRef(metamodel, entity)
                val uniqueRef = cache.putIfAbsent(ref, ref) ?: ref
                entitySets[metamodel].let {
                    checkNotNull(it) { "metamodel not found: $metamodel" }
                    it.add(uniqueRef.entity)
                }
                newRow[metamodel] = uniqueRef
            }
            newRows.add(newRow)
        }
        return EntityStoreImpl(entitySets, newRows)
    }
}
