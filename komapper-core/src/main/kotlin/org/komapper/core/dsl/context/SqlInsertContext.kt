package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Operand
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.Table

internal data class SqlInsertContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val values: List<Pair<Operand.Column, Operand.Parameter>> = listOf()
) : Context<ENTITY> {

    override fun getTables(): List<Table> {
        return listOf(entityMetamodel)
    }

    fun addValues(values: List<Pair<Operand.Column, Operand.Parameter>>): SqlInsertContext<ENTITY> {
        return copy(values = this.values + values)
    }
}
