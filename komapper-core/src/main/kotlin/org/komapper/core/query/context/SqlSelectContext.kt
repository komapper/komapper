package org.komapper.core.query.context

import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.query.data.Criterion

internal data class SqlSelectContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    override val columns: List<ColumnInfo<*>> = listOf(),
    override val joins: List<Join<*>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<ColumnInfo<*>> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val groupBy: List<ColumnInfo<*>> = listOf(),
    val having: List<Criterion> = listOf()
) : SelectContext<ENTITY, SqlSelectContext<ENTITY>> {

    override fun addColumn(column: ColumnInfo<*>): SqlSelectContext<ENTITY> {
        return copy(columns = this.columns + column)
    }

    override fun addColumns(columns: List<ColumnInfo<*>>): SqlSelectContext<ENTITY> {
        return copy(columns = this.columns + columns)
    }

    override fun addJoin(join: Join<*>): SqlSelectContext<ENTITY> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: List<Criterion>): SqlSelectContext<ENTITY> {
        return copy(where = this.where + where)
    }

    fun addHaving(having: List<Criterion>): SqlSelectContext<ENTITY> {
        return copy(having = this.having + having)
    }

    override fun addOrderBy(orderBy: List<ColumnInfo<*>>): SqlSelectContext<ENTITY> {
        return copy(orderBy = this.orderBy + orderBy)
    }

    override fun setLimit(limit: Int): SqlSelectContext<ENTITY> {
        return copy(limit = limit)
    }

    override fun setOffset(offset: Int): SqlSelectContext<ENTITY> {
        return copy(offset = offset)
    }

    override fun setForUpdate(forUpdate: ForUpdate): SqlSelectContext<ENTITY> {
        return copy(forUpdate = forUpdate)
    }

    override fun getReferencingEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }

    override fun getProjectionColumns(): List<ColumnInfo<*>> {
        if (columns.isEmpty()) {
            return entityMetamodel.properties()
        }
        return columns
    }
}
