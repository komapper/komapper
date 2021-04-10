package org.komapper.core.dsl.element

import org.komapper.core.dsl.context.SubqueryContext

internal sealed class Values {
    data class Pairs(val pairs: List<Pair<Operand.Property, Operand.Parameter>>) : Values()
    data class Subquery(val context: SubqueryContext) : Values()
}
