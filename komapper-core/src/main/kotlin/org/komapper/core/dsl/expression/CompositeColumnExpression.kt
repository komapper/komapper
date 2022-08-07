package org.komapper.core.dsl.expression

interface CompositeColumnExpression<T : Any> {
    fun columns(): List<ColumnExpression<*, *>>
    fun arguments(composite: T?): List<Operand.Argument<*, *>>
}
