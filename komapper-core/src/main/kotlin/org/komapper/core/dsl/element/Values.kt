package org.komapper.core.dsl.element

import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.metamodel.PropertyMetamodel

internal sealed class Values {
    data class Pairs(val pairs: List<Pair<PropertyMetamodel<*, *, *>, Operand.Argument<*, *>>>) : Values()
    data class Subquery(val context: SubqueryContext<*>) : Values()
}
