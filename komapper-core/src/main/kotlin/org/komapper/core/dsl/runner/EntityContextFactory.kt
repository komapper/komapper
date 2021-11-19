package org.komapper.core.dsl.runner

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityContext

@ThreadSafe
class EntityContextFactory<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SelectContext<ENTITY, ID, META>,
) {

    fun create(rows: List<Map<EntityMetamodel<*, *, *>, Any>>): EntityContext<ENTITY> {
        val pool: MutableMap<EntityKey, Any> = mutableMapOf()
        val newRows = mutableListOf<Map<EntityMetamodel<*, *, *>, Any>>()
        for (row in rows) {
            val newRow = mutableMapOf<EntityMetamodel<*, *, *>, Any>()
            for ((metamodel, entity) in row) {
                @Suppress("UNCHECKED_CAST")
                metamodel as EntityMetamodel<Any, Any, *>
                val id = metamodel.getId(entity)
                val key = EntityKey(metamodel, id)
                val prev = pool.putIfAbsent(key, entity)
                newRow[metamodel] = prev ?: entity
            }
            newRows.add(newRow)
        }
        return EntityContextImpl(context, pool, newRows)
    }
}
