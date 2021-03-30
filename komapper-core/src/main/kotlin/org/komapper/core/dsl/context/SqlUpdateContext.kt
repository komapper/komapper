package org.komapper.core.dsl.context

import org.komapper.core.config.OptionsImpl
import org.komapper.core.config.SqlUpdateOptions
import org.komapper.core.dsl.data.Criterion
import org.komapper.core.dsl.data.Operand
import org.komapper.core.metamodel.EntityMetamodel

internal data class SqlUpdateContext<ENTITY>(
    val entityMetamodel: EntityMetamodel<ENTITY>,
    val set: List<Pair<Operand.Column, Operand>> = listOf(),
    val where: List<Criterion> = listOf(),
    val options: SqlUpdateOptions = OptionsImpl()
) : Context<ENTITY> {

    override fun getAliasableEntityMetamodels(): List<EntityMetamodel<*>> {
        return listOf(entityMetamodel)
    }

    fun addSet(set: List<Pair<Operand.Column, Operand>>): SqlUpdateContext<ENTITY> {
        return copy(set = this.set + set)
    }

    fun addWhere(where: List<Criterion>): SqlUpdateContext<ENTITY> {
        return copy(where = this.where + where)
    }
}
