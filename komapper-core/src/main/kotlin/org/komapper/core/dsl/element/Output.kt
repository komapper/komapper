package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel

@ThreadSafe
sealed class Output {
    data class Expressions(val expressions: List<ColumnExpression<*, *>>) : Output()
    data class Metamodel(val metamodel: EntityMetamodel<*, *, *>) : Output()

    fun expressions(): List<ColumnExpression<*, *>> {
        return when (this) {
            is Expressions -> this.expressions
            is Metamodel -> this.metamodel.properties()
        }
    }
}
