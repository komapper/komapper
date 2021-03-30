package org.komapper.core.dsl.context

import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlInsertOptions
import org.komapper.core.dsl.data.Operand
import org.komapper.core.metamodel.EntityMetamodel

internal data class SqlInsertContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val values: List<Pair<Operand.Column, Operand.Parameter>> = listOf(),
    val options: SqlInsertOptions = OptionsImpl()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    fun addValues(values: List<Pair<Operand.Column, Operand.Parameter>>): SqlInsertContext<ENTITY> {
        return copy(values = this.values + values)
    }
}
