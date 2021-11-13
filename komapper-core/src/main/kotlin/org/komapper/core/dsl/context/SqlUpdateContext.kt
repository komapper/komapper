package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel

data class SqlUpdateContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: EntityMetamodel<ENTITY, ID, META>,
    val set: List<Pair<ColumnExpression<*, *>, Operand>> = listOf(),
    val where: List<Criterion> = listOf()
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }
}
