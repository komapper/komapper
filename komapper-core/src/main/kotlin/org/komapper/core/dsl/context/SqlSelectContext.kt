package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlSelectContext<ENTITY : Any>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val distinct: Boolean = false,
    override val joins: List<Join<*>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<SortItem> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val groupBy: List<PropertyExpression<*>> = listOf(),
    val having: List<Criterion> = listOf(),
    override val projection: Projection =
        Projection.Entities((listOf(entityMetamodel) + joins.map { it.entityMetamodel }).distinct()),
) : SelectContext<ENTITY, SqlSelectContext<ENTITY>> {

    fun setProperty(property: PropertyExpression<*>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Properties(listOf(property)))
    }

    fun setProperties(properties: List<PropertyExpression<*>>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Properties(properties))
    }

    fun setEntity(entityMetamodel: EntityMetamodel<*>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Entities(listOf(entityMetamodel)))
    }

    fun setEntities(entityMetamodels: List<EntityMetamodel<*>>): SqlSelectContext<ENTITY> {
        return copy(projection = Projection.Entities(entityMetamodels))
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

    override fun addOrderBy(orderBy: List<SortItem>): SqlSelectContext<ENTITY> {
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
