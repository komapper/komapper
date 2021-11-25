package org.komapper.core.dsl.runner

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityStore

@ThreadSafe
class EntityStoreFactory<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
) {

    fun create(rows: List<Map<EntityMetamodel<*, *, *>, Any>>): EntityStore<ENTITY> {
        val cache: MutableMap<EntityKey, Any> = mutableMapOf()
        val newRows = mutableListOf<Map<EntityMetamodel<*, *, *>, Any>>()
        for (row in rows) {
            val newRow = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
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
