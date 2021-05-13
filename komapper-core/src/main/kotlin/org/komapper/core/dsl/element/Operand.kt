package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.expression.ColumnExpression

@ThreadSafe
sealed class Operand {
    data class Column(val expression: ColumnExpression<*, *>) : Operand()
    data class Argument<T : Any, S : Any>(private val expression: ColumnExpression<T, S>, private val exterior: T?) : Operand() {
        val value: Value = if (exterior == null) {
            Value(null, expression.interiorClass)
        } else {
            val interior = expression.unwrap(exterior)
            Value(interior, expression.interiorClass)
        }
    }
}
