package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Criterion
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal interface SelectContext<ENTITY, CONTEXT : SelectContext<ENTITY, CONTEXT>> : Context<ENTITY> {
    val entityMetamodel: EntityMetamodel<ENTITY>
    val columns: List<ColumnInfo<*>>
    val joins: List<Join<*>>
    val where: List<Criterion>
    val orderBy: List<ColumnInfo<*>>
    val offset: Int
    val limit: Int
    val forUpdate: ForUpdate

    fun addColumn(column: ColumnInfo<*>): CONTEXT
    fun addColumns(columns: List<ColumnInfo<*>>): CONTEXT
    fun addJoin(join: Join<*>): CONTEXT
    fun addWhere(where: List<Criterion>): CONTEXT
    fun addOrderBy(orderBy: List<ColumnInfo<*>>): CONTEXT
    fun setLimit(limit: Int): CONTEXT
    fun setOffset(offset: Int): CONTEXT
    fun setForUpdate(forUpdate: ForUpdate): CONTEXT

    override fun getReferencingEntityMetamodels(): List<EntityMetamodel<*>>
    fun getProjectionColumns(): List<ColumnInfo<*>>
}
