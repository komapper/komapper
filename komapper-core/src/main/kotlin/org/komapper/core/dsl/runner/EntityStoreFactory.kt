package org.komapper.core.dsl.runner

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityStore
import org.komapper.core.dsl.query.EntityStoreImpl

@ThreadSafe
class EntityStoreFactory<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
) {

    fun create(rows: List<Map<EntityMetamodel<*, *, *>, Any>>): EntityStore {
        val cache: MutableMap<EntityKey, Any> = mutableMapOf()
        val newRows = ArrayList<Map<EntityMetamodel<*, *, *>, Any>>(rows.size)
        for (row in rows) {
            val newRow = LinkedHashMap<EntityMetamodel<*, *, *>, Any>(row.size)
            for ((metamodel, entity) in row) {
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val id = metamodel.id(entity)
                val key = EntityKey(metamodel, id)
                val prev = cache.putIfAbsent(key, entity)
                newRow[metamodel] = prev ?: entity
            }
            newRows.add(newRow)
        }
        return EntityStoreImpl(context, newRows)
    }
}
