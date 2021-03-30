package org.komapper.core.dsl.context

import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlDeleteOptions
import org.komapper.core.dsl.data.Criterion
import org.komapper.core.metamodel.EntityMetamodel

internal data class SqlDeleteContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val where: List<Criterion> = listOf(),
    val options: SqlDeleteOptions = OptionsImpl()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    fun addWhere(where: List<Criterion>): SqlDeleteContext<ENTITY> {
        return copy(where = this.where + where)
    }
}
