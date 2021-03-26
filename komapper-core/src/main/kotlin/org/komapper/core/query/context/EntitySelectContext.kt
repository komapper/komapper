package org.komapper.core.query.context

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal class EntitySelectContext<ENTITY>(override val entityMetamodel: EntityMetamodel<ENTITY>) : SelectContext<ENTITY> {
    override val columns = mutableListOf<ColumnInfo<*>>()
    override val joins = JoinsContext()
    override val where = FilterContext()
    override val orderBy = OrderByContext()
    override var offset: Int = -1
    override var limit: Int = -1
    override val forUpdate = ForUpdateContext()
    val associatorMap = AssociatorMap()

    override fun getEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }

    override fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>> {
        val list = listOf(entityMetamodel) + associatorMap.flatMap {
            val (e1, e2) = it.key
            listOf(e1, e2)
        }
        return list.distinct()
    }

    override fun getProjectionColumns(): List<ColumnInfo<*>> {
        if (columns.isEmpty()) {
            return getProjectionEntityMetamodels().flatMap { it.properties() }
        }
        return columns
    }
}
