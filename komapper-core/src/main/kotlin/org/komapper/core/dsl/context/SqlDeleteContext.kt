package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.Table

internal data class SqlDeleteContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val where: List<Criterion> = listOf()
) : Context<ENTITY> {

    override fun getTables(): List<Table> {
        return listOf(entityMetamodel)
    }

    fun addWhere(where: List<Criterion>): SqlDeleteContext<ENTITY> {
        return copy(where = this.where + where)
    }
}
