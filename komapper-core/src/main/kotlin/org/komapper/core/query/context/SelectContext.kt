package org.komapper.core.query.context

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal interface SelectContext<ENTITY> : Context<ENTITY> {
    val entityMetamodel: EntityMetamodel<ENTITY>
    val columns: MutableList<ColumnInfo<*>>
    val joins: JoinsContext
    val where: FilterContext
    val orderBy: OrderByContext
    var offset: Int
    var limit: Int
    val forUpdate: ForUpdateContext

    override fun getEntityMetamodels(): List<EntityMetamodel<*>>
    fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>>
    fun getProjectionColumns(): List<ColumnInfo<*>>
}
