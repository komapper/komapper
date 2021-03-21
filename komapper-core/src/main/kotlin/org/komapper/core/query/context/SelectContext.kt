package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel

internal class SelectContext<ENTITY>(val entityMetamodel: EntityMetamodel<ENTITY>) : Context<ENTITY> {
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

    fun getProjectionPropertyMetamodels(): List<PropertyMetamodel<*, *>> {
        return entityMetamodel.properties()
    }

    fun getJoinTargets(): List<EntityMetamodel<*>> {
        return joins.map { it.entityMetamodel }
    }

    fun getProjectionTargets(): List<EntityMetamodel<*>> {
        val list = listOf(entityMetamodel) + associatorMap.flatMap {
            val (e1, e2) = it.key
            listOf(e1, e2)
        }
        return list.distinct()
    }
}
