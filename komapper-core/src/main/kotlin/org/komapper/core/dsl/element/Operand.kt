package org.komapper.core.dsl.element

import org.komapper.core.dsl.expression.ColumnExpression
import kotlin.reflect.KClass

sealed class Operand {
    data class Column(val expression: ColumnExpression<*>) : Operand()
    data class Argument(val klass: KClass<*>, val value: Any?) : Operand()
}
