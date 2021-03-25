package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel

internal class SqlSelectContext<ENTITY>(override val entityMetamodel: EntityMetamodel<ENTITY>) : SelectContext<ENTITY> {
    override val projections = mutableListOf<PropertyMetamodel<*, *>>()
    override val joins = JoinsContext()
    override val where = WhereContext()
    override val orderBy = OrderByContext()
    override var offset: Int = -1
    override var limit: Int = -1
    override val forUpdate = ForUpdateContext()

    override fun getEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }

    // TODO
    override fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    override fun getProjectionPropertyMetamodels(): List<PropertyMetamodel<*, *>> {
        if (projections.isEmpty()) {
            return getProjectionEntityMetamodels().flatMap { it.properties() }
        }
        return projections
    }
}
