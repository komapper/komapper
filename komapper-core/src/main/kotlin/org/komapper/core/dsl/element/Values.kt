package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.ValuesDeclaration

@ThreadSafe
sealed class Values<ENTITY : Any> {
    data class Declarations<ENTITY : Any>(val declarations: List<ValuesDeclaration<ENTITY>>) : Values<ENTITY>()
    data class Subquery<ENTITY : Any>(val expression: SubqueryExpression<*>) : Values<ENTITY>()
}
