package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Association
import org.komapper.core.dsl.element.Associator
import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expr.NamedSortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class EntitySelectContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    override val joins: List<Join<*>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<NamedSortItem<*>> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val associatorMap: Map<Association, Associator<Any, Any>> = mapOf(),
    override val projection: Projection.Entities = Projection.Entities(
        (listOf(entityMetamodel) + associatorMap.keys.flatMap { listOf(it.first, it.second) }).distinct()
    )

) : SelectContext<ENTITY, EntitySelectContext<ENTITY>> {

    override fun addJoin(join: Join<*>): EntitySelectContext<ENTITY> {
        return copy(joins = this.joins + join)
    }

    override fun addWhere(where: List<Criterion>): EntitySelectContext<ENTITY> {
        return copy(where = this.where + where)
    }

    override fun addOrderBy(orderBy: List<NamedSortItem<*>>): EntitySelectContext<ENTITY> {
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
        val newProjection = Projection.Entities(
            (projection.values + listOf(association.first, association.second)).distinct()
        )
        return copy(projection = newProjection, associatorMap = this.associatorMap + (association to associator))
    }
}
