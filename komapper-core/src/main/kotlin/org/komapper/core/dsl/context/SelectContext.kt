package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.ForUpdate
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Projection
import org.komapper.core.metamodel.Column
import org.komapper.core.metamodel.Table

internal interface SelectContext<ENTITY, CONTEXT : SelectContext<ENTITY, CONTEXT>> : Context<ENTITY> {
    val joins: List<Join<*>>
    val where: List<Criterion>
    val orderBy: List<Column<*>>
    val offset: Int
    val limit: Int
    val forUpdate: ForUpdate
    val projection: Projection

    fun addJoin(join: Join<*>): CONTEXT
    fun addWhere(where: List<Criterion>): CONTEXT
    fun addOrderBy(orderBy: List<Column<*>>): CONTEXT
    fun setLimit(limit: Int): CONTEXT
    fun setOffset(offset: Int): CONTEXT
    fun setForUpdate(forUpdate: ForUpdate): CONTEXT

    override fun getTables(): List<Table> {
        return listOf(entityMetamodel) + joins.map { it.entityMetamodel }
    }
}
