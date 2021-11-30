package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@Scope
class AssignmentScope<ENTITY : Any>(
    private val assignments: MutableList<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> = mutableListOf()
) {

    infix fun <T : Any> PropertyMetamodel<ENTITY, T, *>.set(value: T?) {
        val right = Operand.Argument(this, value)
        assignments.add(this to right)
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.set(operand: ColumnExpression<T, S>) {
        val right = Operand.Column(operand)
        assignments.add(this to right)
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.setIfNotNull(value: T?) {
        if (value == null) return
        val right = Operand.Argument(this, value)
        assignments.add(this to right)
    }
}
