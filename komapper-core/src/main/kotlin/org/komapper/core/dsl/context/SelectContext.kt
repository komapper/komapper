package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.expr.NamedSortItem
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal interface SelectContext<ENTITY, CONTEXT : SelectContext<ENTITY, CONTEXT>> : Context<ENTITY> {
    val joins: List<Join<*>>
    val where: List<Criterion>
    val orderBy: List<NamedSortItem<*>>
    val offset: Int
    val limit: Int
    val forUpdate: ForUpdate
    val projection: Projection

    fun addJoin(join: Join<*>): CONTEXT
    fun addWhere(where: List<Criterion>): CONTEXT
    fun addOrderBy(orderBy: List<NamedSortItem<*>>): CONTEXT
    fun setLimit(limit: Int): CONTEXT
    fun setOffset(offset: Int): CONTEXT
    fun setForUpdate(forUpdate: ForUpdate): CONTEXT

    override fun getEntityExpressions(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }
}
