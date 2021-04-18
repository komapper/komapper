package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.expression.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlUpdateContext<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val entityMetamodel: EntityMetamodel<ENTITY, META>,
    val set: List<Pair<PropertyExpression<*>, Operand>> = listOf(),
    val where: List<Criterion> = listOf()
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(entityMetamodel)
    }
}
