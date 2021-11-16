package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.declaration.ValuesDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression

@ThreadSafe
sealed class Values {
    data class Declarations(val declarations: List<ValuesDeclaration<*>>) : Values()
    data class Subquery(val expression: SubqueryExpression<*>) : Values()
}
