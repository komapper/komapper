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

    fun create(rows: List<MutableMap<EntityMetamodel<*, *, *>, Any>>): EntityStore {
        val cache: MutableMap<EntityKey, Any> = mutableMapOf()
        for (row in rows) {
            for ((metamodel, entity) in row) {
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val id = metamodel.id(entity)
                val key = EntityKey(metamodel, id)
                val prev = cache.putIfAbsent(key, entity)
                row[metamodel] = prev ?: entity
            }
        }
        return EntityStoreImpl(context, rows)
    }
}
