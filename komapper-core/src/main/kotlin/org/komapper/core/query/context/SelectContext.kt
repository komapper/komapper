package org.komapper.core.query.context

import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel

internal interface SelectContext<ENTITY> : Context<ENTITY> {
    val entityMetamodel: EntityMetamodel<ENTITY>
    val projections: MutableList<PropertyMetamodel<*, *>>
    val joins: JoinsContext
    val where: WhereContext
    val orderBy: OrderByContext
    var offset: Int
    var limit: Int
    val forUpdate: ForUpdateContext

    override fun getEntityMetamodels(): List<EntityMetamodel<*>>
    fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>>
    fun getProjectionPropertyMetamodels(): List<PropertyMetamodel<*, *>>
}
