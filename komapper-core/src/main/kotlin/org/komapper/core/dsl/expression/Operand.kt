package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe
import org.komapper.core.Value

@ThreadSafe
sealed class Operand {
    abstract val masking: Boolean
    data class Column(val expression: ColumnExpression<*, *>) : Operand() {
        override val masking: Boolean get() = expression.masking
    }
    data class Argument<T : Any, S : Any>(val expression: ColumnExpression<T, S>, val exterior: T?) : Operand() {
        override val masking: Boolean get() = expression.masking
        val value: Value<S> get() {
            val interior = if (exterior == null) null else expression.unwrap(exterior)
            return Value(interior, expression.interiorClass, expression.masking)
        }
    }
    data class Escape(val expression: ColumnExpression<*, *>, val escapeExpression: EscapeExpression) : Operand() {
        override val masking: Boolean get() = expression.masking
    }
    data class Subquery(val subqueryExpression: SubqueryExpression<*>) : Operand() {
        override val masking: Boolean = false
    }
}
