package org.komapper.core.dsl.expr

internal sealed class NamedSortItem<T : Any> : PropertyExpression<T> {
    abstract val expression: PropertyExpression<T>

    data class Asc<T : Any>(override val expression: PropertyExpression<T>) :
        NamedSortItem<T>(),
        PropertyExpression<T> by expression

    data class Desc<T : Any>(override val expression: PropertyExpression<T>) :
        NamedSortItem<T>(),
        PropertyExpression<T> by expression
}
