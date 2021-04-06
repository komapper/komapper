package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlUpdateContext<ENTITY>(
    override val entityMetamodel: EntityMetamodel<ENTITY>,
    val set: List<Pair<Operand.Property, Operand>> = listOf(),
    val where: List<Criterion> = listOf()
) : Context<ENTITY> {

    override fun getEntityExpressions(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    fun addSet(set: List<Pair<Operand.Property, Operand>>): SqlUpdateContext<ENTITY> {
        return copy(set = this.set + set)
    }

    fun addWhere(where: List<Criterion>): SqlUpdateContext<ENTITY> {
        return copy(where = this.where + where)
    }
}
