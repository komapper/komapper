package org.komapper.core.query.context

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal class SqlSelectContext<ENTITY>(override val entityMetamodel: EntityMetamodel<ENTITY>) : SelectContext<ENTITY> {
    override val columns = mutableListOf<ColumnInfo<*>>()
    override val joins = JoinsContext()
    override val where = FilterContext()
    override val orderBy = OrderByContext()
    override var offset: Int = -1
    override var limit: Int = -1
    override val forUpdate = ForUpdateContext()
    val groupBy = mutableListOf<ColumnInfo<*>>()
    val having = FilterContext()

    override fun getEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }

    // TODO
    override fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    override fun getProjectionColumns(): List<ColumnInfo<*>> {
        if (columns.isEmpty()) {
            return getProjectionEntityMetamodels().flatMap { it.properties() }
        }
        return columns
    }
}
