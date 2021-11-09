package org.komapper.core.dsl.query

import org.komapper.core.dsl.context.EntitySelectContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.EntityKey

class EntityAggregateFactory<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: EntitySelectContext<ENTITY, ID, META>,
) {

    fun create(rows: List<Map<EntityKey, Any>>): EntityAggregate<ENTITY> {
        return EntityAggregateImpl(context, rows)
    }
}
