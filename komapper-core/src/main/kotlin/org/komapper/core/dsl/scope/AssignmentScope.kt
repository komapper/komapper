package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel

/**
 * Provides operators for SET and VALUES clauses.
 */
@Scope
class AssignmentScope<ENTITY : Any>(
    private val assignments: MutableList<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> = mutableListOf()
) {

    /**
     * The `=` operator.
     */
    infix fun <T : Any> PropertyMetamodel<ENTITY, T, *>.eq(value: T?) {
        val right = Operand.Argument(this, value)
        assignments.add(this to right)
    }

    /**
     * The `=` operator.
     */
    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.eq(operand: ColumnExpression<T, S>) {
        val right = Operand.Column(operand)
        assignments.add(this to right)
    }

    /**
     * Behaves like the `=` operator only if the value is not `null`.
     * If the value is `null`, the assignment is ignored.
     */
    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.eqIfNotNull(value: T?) {
        if (value == null) return
        val right = Operand.Argument(this, value)
        assignments.add(this to right)
    }
}
