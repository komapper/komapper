package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expr.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlInsertContext<ENTITY : Any>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val values: List<Pair<Operand.Property, Operand.Parameter>> = listOf()
) : Context<ENTITY> {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }

    fun addValues(values: List<Pair<Operand.Property, Operand.Parameter>>): SqlInsertContext<ENTITY> {
        return copy(values = this.values + values)
    }
}
