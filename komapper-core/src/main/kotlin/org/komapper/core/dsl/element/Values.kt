package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@ThreadSafe
sealed class Values {
    data class Pairs(val pairs: List<Pair<PropertyMetamodel<*, *, *>, Operand>>) : Values()
    data class Subquery(val expression: SubqueryExpression<*>) : Values()
}
