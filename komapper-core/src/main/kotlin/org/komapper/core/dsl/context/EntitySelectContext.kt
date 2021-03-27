package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Criterion
import org.komapper.core.dsl.query.Associator
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntitySelectContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    override val columns: List<ColumnInfo<*>> = listOf(),
    override val joins: List<Join<*>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<ColumnInfo<*>> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val associatorMap: Map<Association, Associator<Any, Any>> = mapOf()
) : SelectContext<ENTITY, EntitySelectContext<ENTITY>> {

    override fun addColumn(column: ColumnInfo<*>): EntitySelectContext<ENTITY> {
        return copy(columns = this.columns + column)
    }

    override fun addColumns(columns: List<ColumnInfo<*>>): EntitySelectContext<ENTITY> {
        return copy(columns = this.columns + columns)
    }

    override fun addJoin(join: Join<*>): EntitySelectContext<ENTITY> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: List<Criterion>): EntitySelectContext<ENTITY> {
        return copy(where = this.where + where)
    }

    override fun addOrderBy(orderBy: List<ColumnInfo<*>>): EntitySelectContext<ENTITY> {
        return copy(orderBy = this.orderBy + orderBy)
    }

    override fun setLimit(limit: Int): EntitySelectContext<ENTITY> {
        return copy(limit = limit)
    }

    override fun setOffset(offset: Int): EntitySelectContext<ENTITY> {
        return copy(offset = offset)
    }

    override fun setForUpdate(forUpdate: ForUpdate): EntitySelectContext<ENTITY> {
        return copy(forUpdate = forUpdate)
    }

    fun putAssociator(association: Association, associator: Associator<Any, Any>): EntitySelectContext<ENTITY> {
        return copy(associatorMap = this.associatorMap + (association to associator))
    }

    override fun getReferencingEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }

    fun getProjectionEntityMetamodels(): List<EntityMetamodel<*>> {
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
