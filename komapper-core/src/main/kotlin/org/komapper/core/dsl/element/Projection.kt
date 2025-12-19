package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
sealed class Projection {
    data class Expressions(val expressions: List<ColumnExpression<*, *>>) : Projection()
    data class Metamodels(val metamodels: Set<EntityMetamodel<*, *, *>>) : Projection()

    fun expressions(projectionPredicate: (ColumnExpression<*, *>) -> Boolean = { true }): List<ColumnExpression<*, *>> {
        return when (this) {
            is Expressions -> this.expressions.filter(projectionPredicate)

            is Metamodels -> this.metamodels.flatMap {
                it.properties().filter(projectionPredicate)
            }
        }
    }

    fun metamodels(): Set<EntityMetamodel<*, *, *>> {
        return when (this) {
            is Expressions -> emptySet()
            is Metamodels -> this.metamodels
        }
    }
}
