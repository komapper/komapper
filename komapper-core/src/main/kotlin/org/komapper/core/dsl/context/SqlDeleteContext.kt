package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlDeleteContext<ENTITY : Any>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val where: List<Criterion> = listOf()
) : Context<ENTITY> {

    override fun getEntityExpressions(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    fun addWhere(where: List<Criterion>): SqlDeleteContext<ENTITY> {
        return copy(where = this.where + where)
    }
}
