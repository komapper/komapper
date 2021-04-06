package org.komapper.core.dsl.element

internal sealed class Operand {
    data class Parameter(val column: org.komapper.core.metamodel.Column<*>, val value: Any?) : Operand()
    data class Column(val column: org.komapper.core.metamodel.Column<*>) : Operand()
}
