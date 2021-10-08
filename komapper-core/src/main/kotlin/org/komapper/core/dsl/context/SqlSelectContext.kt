package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class SqlSelectContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    override val target: META,
    override val projection: Projection = Projection.Metamodels(listOf(target)),
    override val joins: List<Join<*, *>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<SortItem> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val groupBy: List<ColumnExpression<*, *>> = listOf(),
    val having: List<Criterion> = listOf(),
    val distinct: Boolean = false,
) : SelectContext<ENTITY, ID, META, SqlSelectContext<ENTITY, ID, META>>, SubqueryContext {

    fun setProjection(vararg expressions: ColumnExpression<*, *>): SqlSelectContext<ENTITY, ID, META> {
        return copy(projection = Projection.Expressions(expressions.toList()))
    }

    fun setProjection(vararg metamodels: EntityMetamodel<*, *, *>): SqlSelectContext<ENTITY, ID, META> {
        return copy(projection = Projection.Metamodels(metamodels.toList()))
    }

    override fun addJoin(join: Join<*, *>): SqlSelectContext<ENTITY, ID, META> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: List<Criterion>): SqlSelectContext<ENTITY, ID, META> {
        return copy(where = this.where + where)
    }

    override fun addOrderBy(orderBy: List<SortItem>): SqlSelectContext<ENTITY, ID, META> {
        return copy(orderBy = this.orderBy + orderBy)
    }

    override fun setLimit(limit: Int): SqlSelectContext<ENTITY, ID, META> {
        return copy(limit = limit)
    }

    override fun setOffset(offset: Int): SqlSelectContext<ENTITY, ID, META> {
        return copy(offset = offset)
    }

    override fun setForUpdate(forUpdate: ForUpdate): SqlSelectContext<ENTITY, ID, META> {
        return copy(forUpdate = forUpdate)
    }
}
