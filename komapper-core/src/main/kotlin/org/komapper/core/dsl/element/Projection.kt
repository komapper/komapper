package org.komapper.core.dsl.element

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

internal sealed class Projection {
    data class Expressions(val expressions: List<ColumnExpression<*>>) : Projection()
    data class Metamodels(val metamodels: List<EntityMetamodel<*, *, *>>) : Projection()

    fun expressions(): List<ColumnExpression<*>> {
        return when (this) {
            is Expressions -> this.expressions
            is Metamodels -> this.metamodels.flatMap { it.properties() }
        }
    }
}
