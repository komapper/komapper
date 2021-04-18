package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlSelectContext<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    override val target: META,
    override val projection: Projection = Projection.Entities(listOf(target)),
    override val joins: List<Join<*, *>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<SortItem> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val groupBy: List<PropertyExpression<*>> = listOf(),
    val having: List<Criterion> = listOf(),
    val distinct: Boolean = false,
) : SelectContext<ENTITY, META, SqlSelectContext<ENTITY, META>> {

    fun setProperties(vararg properties: PropertyExpression<*>): SqlSelectContext<ENTITY, META> {
        return copy(projection = Projection.Properties(properties.toList()))
    }

    fun setEntities(vararg entityMetamodels: EntityMetamodel<*, *>): SqlSelectContext<ENTITY, META> {
        return copy(projection = Projection.Entities(entityMetamodels.toList()))
    }

    override fun addJoin(join: Join<*, *>): SqlSelectContext<ENTITY, META> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: List<Criterion>): SqlSelectContext<ENTITY, META> {
        return copy(where = this.where + where)
    }

    override fun addOrderBy(orderBy: List<SortItem>): SqlSelectContext<ENTITY, META> {
        return copy(orderBy = this.orderBy + orderBy)
    }

    override fun setLimit(limit: Int): SqlSelectContext<ENTITY, META> {
        return copy(limit = limit)
    }

    override fun setOffset(offset: Int): SqlSelectContext<ENTITY, META> {
        return copy(offset = offset)
    }

    override fun setForUpdate(forUpdate: ForUpdate): SqlSelectContext<ENTITY, META> {
        return copy(forUpdate = forUpdate)
    }
}
