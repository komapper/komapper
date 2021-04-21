package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Criterion
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlDeleteContext<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val where: List<Criterion> = listOf()
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }
}
