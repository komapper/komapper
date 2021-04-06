package org.komapper.core.dsl.expr

import org.komapper.core.dsl.element.Operand
import org.komapper.core.metamodel.Column

internal sealed class StringFunction : Column<String> {
    internal data class Concat(
        val c: Column<String>,
        val left: Operand,
        val right: Operand
    ) :
        Column<String> by c, StringFunction()
}
