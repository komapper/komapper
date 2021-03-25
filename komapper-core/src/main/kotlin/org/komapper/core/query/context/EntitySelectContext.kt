package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel

internal class EntitySelectContext<ENTITY>(val entityMetamodel: EntityMetamodel<ENTITY>) : Context<ENTITY> {
    val projections = mutableListOf<PropertyMetamodel<*, *>>()
    val joins = JoinsContext()
    val associatorMap = AssociatorMap()
    val where = WhereContext()
    val orderBy = OrderByContext()
    var offset: Int = -1
    var limit: Int = -1
    val forUpdate = ForUpdateContext()

    override fun getEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }

    fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>> {
        val list = listOf(entityMetamodel) + associatorMap.flatMap {
            val (e1, e2) = it.key
            listOf(e1, e2)
        }
        return list.distinct()
    }

    fun getProjectionPropertyMetamodels(): List<PropertyMetamodel<*, *>> {
        if (projections.isEmpty()) {
            return getProjectionEntityMetamodels().flatMap { it.properties() }
        }
        return projections
    }
}
