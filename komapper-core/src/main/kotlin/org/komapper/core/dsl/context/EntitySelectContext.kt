package org.komapper.core.dsl.context

import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expression.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class EntitySelectContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    override val target: META,
    override val projection: Projection.Metamodels = Projection.Metamodels(listOf(target)),
    override val joins: List<Join<*, *, *>> = listOf(),
    override val where: List<WhereDeclaration> = listOf(),
    override val orderBy: List<SortItem> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    override val distinct: Boolean = false,
) : SelectContext<ENTITY, ID, META, EntitySelectContext<ENTITY, ID, META>> {

    override fun addJoin(join: Join<*, *, *>): EntitySelectContext<ENTITY, ID, META> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: WhereDeclaration): EntitySelectContext<ENTITY, ID, META> {
        return copy(where = this.where + where)
    }

    override fun addOrderBy(orderBy: List<SortItem>): EntitySelectContext<ENTITY, ID, META> {
        return copy(orderBy = this.orderBy + orderBy)
    }

    override fun setLimit(limit: Int): EntitySelectContext<ENTITY, ID, META> {
        return copy(limit = limit)
    }

    override fun setOffset(offset: Int): EntitySelectContext<ENTITY, ID, META> {
        return copy(offset = offset)
    }

    override fun setForUpdate(forUpdate: ForUpdate): EntitySelectContext<ENTITY, ID, META> {
        return copy(forUpdate = forUpdate)
    }

    fun addProjectionMetamodels(
        metamodels: List<EntityMetamodel<*, *, *>>,
    ): EntitySelectContext<ENTITY, ID, META> {
        val newProjection = Projection.Metamodels((projection.metamodels + metamodels).distinct())
        return copy(projection = newProjection)
    }

    fun asSqlSelectContext(): SqlSelectContext<ENTITY, ID, META> {
        return SqlSelectContext(
            target = target,
            joins = joins,
            where = where,
            orderBy = orderBy,
            offset = offset,
            limit = limit,
            forUpdate = forUpdate,
            distinct = distinct
        )
    }
}
