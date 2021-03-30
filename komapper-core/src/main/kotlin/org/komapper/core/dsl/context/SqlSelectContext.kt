package org.komapper.core.dsl.context

import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlSelectOptions
import org.komapper.core.dsl.data.Criterion
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal data class SqlSelectContext<ENTITY>(
    override val from: EntityMetamodel<ENTITY>,
    override val joins: List<Join<*>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<ColumnInfo<*>> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val groupBy: List<ColumnInfo<*>> = listOf(),
    val having: List<Criterion> = listOf(),
    override val projection: Projection =
        Projection.Tables(listOf(from) + joins.map { it.entityMetamodel }),
    val options: SqlSelectOptions = OptionsImpl(allowEmptyWhereClause = true)
) : SelectContext<ENTITY, SqlSelectContext<ENTITY>> {

    fun setColumn(column: ColumnInfo<*>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Columns(listOf(column)))
    }

    fun setColumns(columns: List<ColumnInfo<*>>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Columns(columns))
    }

    fun setTable(entityMetamodel: EntityMetamodel<*>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Tables(listOf(entityMetamodel)))
    }

    fun setTables(entityMetamodels: List<EntityMetamodel<*>>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Tables(entityMetamodels))
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
}
