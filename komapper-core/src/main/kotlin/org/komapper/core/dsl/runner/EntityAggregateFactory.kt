package org.komapper.core.dsl.runner

import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.element.Association
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityAggregate
import org.komapper.core.dsl.query.EntityAggregateImpl

class EntityAggregateFactory(
    private val context: EntitySelectContext<*, *, *>
) {

    private val associations: MutableMap<Association, MutableMap<Any, MutableSet<Any>>> = mutableMapOf()

    fun create(rows: List<Map<EntityKey, Any>>): EntityAggregate {
        // hold only unique entities
        val pool: MutableMap<EntityKey, Any> = mutableMapOf()
        for (row in rows) {
            val entityKeys: MutableMap<EntityMetamodel<*, *, *>, EntityKey> = mutableMapOf()
            for ((key, entity) in row) {
                pool.putIfAbsent(key, entity)
                entityKeys[key.entityMetamodel] = key
            }
            for ((first, second) in context.associations) {
                val key1 = entityKeys[first]
                val key2 = entityKeys[second]
                if (key1 != null) {
                    val entity1 = pool[key1]!!
                    val entity2 = pool[key2]
                    associate(first to second, entity1, entity2)
                }
                if (key2 != null) {
                    val entity1 = pool[key1]
                    val entity2 = pool[key2]!!
                    associate(second to first, entity2, entity1)
                }
            }
        }
        return EntityAggregateImpl(associations)
    }

    private fun associate(
        association: Association,
        key: Any,
        value: Any?
    ) {
        val map = associations.computeIfAbsent(association) { mutableMapOf() }
        val set = map.computeIfAbsent(key) { mutableSetOf() }
        if (value != null) {
            set.add(value)
        }
    }
}
