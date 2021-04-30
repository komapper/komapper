package org.komapper.core.dsl.element

import org.komapper.core.Value
import org.komapper.core.dsl.expression.ColumnExpression
import kotlin.reflect.cast

sealed class Operand {
    data class Column(val expression: ColumnExpression<*, *>) : Operand()
    data class ExteriorArgument<T : Any, S : Any>(val expression: ColumnExpression<T, S>, val value: Any?) : Operand() {
        fun asValue(): Value {
            return if (value == null) {
                Value(null, expression.interiorClass)
            } else {
                val exterior = expression.exteriorClass.cast(value)
                val interior = expression.unwrap(exterior)
                Value(interior, expression.interiorClass)
            }
        }
    }
    data class InteriorArgument<T : Any, S : Any>(val expression: ColumnExpression<T, S>, val value: Any?) : Operand() {
        fun asValue(): Value {
            return if (value == null) {
                Value(null, expression.interiorClass)
            } else {
                Value(value, expression.interiorClass)
            }
        }
    }
}
