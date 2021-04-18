package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Association
import org.komapper.core.dsl.element.Associator
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntitySelectContext<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    override val target: META,
    override val projection: Projection.Entities = Projection.Entities(listOf(target)),
    override val joins: List<Join<*, *>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<SortItem> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val associatorMap: Map<Association, Associator<Any, Any>> = mapOf()
) : SelectContext<ENTITY, META, EntitySelectContext<ENTITY, META>> {

    override fun addJoin(join: Join<*, *>): EntitySelectContext<ENTITY, META> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: List<Criterion>): EntitySelectContext<ENTITY, META> {
        return copy(where = this.where + where)
    }

    override fun addOrderBy(orderBy: List<SortItem>): EntitySelectContext<ENTITY, META> {
        return copy(orderBy = this.orderBy + orderBy)
    }

    override fun setLimit(limit: Int): EntitySelectContext<ENTITY, META> {
        return copy(limit = limit)
    }

    override fun setOffset(offset: Int): EntitySelectContext<ENTITY, META> {
        return copy(offset = offset)
    }

    override fun setForUpdate(forUpdate: ForUpdate): EntitySelectContext<ENTITY, META> {
        return copy(forUpdate = forUpdate)
    }

    fun putAssociator(association: Association, associator: Associator<Any, Any>): EntitySelectContext<ENTITY, META> {
        val newProjection = Projection.Entities(
            (projection.values + listOf(association.first, association.second)).distinct()
        )
        return copy(projection = newProjection, associatorMap = this.associatorMap + (association to associator))
    }

    fun asSqlSelectContext(): SqlSelectContext<ENTITY, META> {
        return SqlSelectContext(
            target = target,
            joins = joins,
            where = where,
            orderBy = orderBy,
            offset = offset,
            limit = limit,
            forUpdate = forUpdate
        )
    }
}
