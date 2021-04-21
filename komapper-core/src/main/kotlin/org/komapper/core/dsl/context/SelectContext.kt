package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.dsl.element.SortItem
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal interface SelectContext<
    ENTITY : Any,
    ID,
    META : EntityMetamodel<ENTITY, ID, META>,
    CONTEXT : SelectContext<ENTITY, ID, META, CONTEXT>> : Context {

    val target: META
    val projection: Projection
    val joins: List<Join<*, *, *>>
    val where: List<Criterion>
    val orderBy: List<SortItem>
    val offset: Int
    val limit: Int
    val forUpdate: ForUpdate

    fun addJoin(join: Join<*, *, *>): CONTEXT
    fun addWhere(where: List<Criterion>): CONTEXT
    fun addOrderBy(orderBy: List<SortItem>): CONTEXT
    fun setLimit(limit: Int): CONTEXT
    fun setOffset(offset: Int): CONTEXT
    fun setForUpdate(forUpdate: ForUpdate): CONTEXT

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target) + joins.map { it.entityMetamodel }
    }
}
