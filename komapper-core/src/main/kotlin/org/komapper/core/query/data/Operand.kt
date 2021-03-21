package org.komapper.core.query.data

import org.komapper.core.metamodel.PropertyMetamodel

internal sealed class Operand {
    data class Parameter(val metamodel: PropertyMetamodel<*, *>, val value: Any?) : Operand()
    data class Property(val metamodel: PropertyMetamodel<*, *>) : Operand()
}
