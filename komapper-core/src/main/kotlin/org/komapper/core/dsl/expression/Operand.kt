package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe
import org.komapper.core.Value

@ThreadSafe
sealed class Operand {
    data class Column(val expression: ColumnExpression<*, *>) : Operand()
    data class Argument<T : Any, S : Any>(private val expression: ColumnExpression<T, S>, private val exterior: T?) : Operand() {
        val value: Value get() = if (exterior == null) {
            Value(null, expression.interiorClass)
        } else {
            val interior = expression.unwrap(exterior)
            Value(interior, expression.interiorClass)
        }
    }
}
