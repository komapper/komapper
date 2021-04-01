package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Criterion
import org.komapper.core.dsl.query.Associator
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel

internal data class EntitySelectContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    override val joins: List<Join<*>> = listOf(),
    override val where: List<Criterion> = listOf(),
    override val orderBy: List<ColumnInfo<*>> = listOf(),
    override val offset: Int = -1,
    override val limit: Int = -1,
    override val forUpdate: ForUpdate = ForUpdate(),
    val associatorMap: Map<Association, Associator<Any, Any>> = mapOf(),
    override val projection: Projection = Projection.Tables(
        setOf(entityMetamodel) + associatorMap.keys.flatMap { setOf(it.first, it.second) }
    )

) : SelectContext<ENTITY, EntitySelectContext<ENTITY>> {

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
        val newProjection = when (projection) {
            is Projection.Columns -> error("cannot happen.")
            is Projection.Tables -> Projection.Tables(
                projection.values + setOf(association.first, association.second)
            )
        }
        return copy(projection = newProjection, associatorMap = this.associatorMap + (association to associator))
    }
}
