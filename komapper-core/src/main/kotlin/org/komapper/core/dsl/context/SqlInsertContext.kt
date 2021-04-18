package org.komapper.core.dsl.context

import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.EntityExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal data class SqlInsertContext<ENTITY : Any, META : EntityMetamodel<ENTITY, META>>(
    val target: META,
    val values: Values = Values.Pairs(emptyList())
) : Context {

    override fun getEntityExpressions(): Set<EntityExpression<*>> {
        return setOf(target)
    }
}
