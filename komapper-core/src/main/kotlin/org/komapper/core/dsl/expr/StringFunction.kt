package org.komapper.core.dsl.expr

import org.komapper.core.dsl.element.Operand
import org.komapper.core.metamodel.ColumnInfo

internal sealed class StringFunction : ColumnInfo<String> {
    internal data class Concat(
        val c: ColumnInfo<String>,
        val left: Operand,
        val right: Operand
    ) :
        ColumnInfo<String> by c, StringFunction()
}
