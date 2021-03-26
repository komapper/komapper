package org.komapper.core.query.data

import org.komapper.core.metamodel.ColumnInfo

internal sealed class Operand {
    data class Parameter(val columnInfo: ColumnInfo<*>, val value: Any?) : Operand()
    data class Column(val columnInfo: ColumnInfo<*>) : Operand()
}
