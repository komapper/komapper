package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Operand
import org.komapper.core.metamodel.EntityMetamodel

internal data class SqlInsertContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val values: List<Pair<Operand.Column, Operand.Parameter>> = listOf()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    fun addValues(values: List<Pair<Operand.Column, Operand.Parameter>>): SqlInsertContext<ENTITY> {
        return copy(values = this.values + values)
    }
}
