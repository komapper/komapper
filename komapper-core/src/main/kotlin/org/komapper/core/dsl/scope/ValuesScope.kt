package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@Scope
class ValuesScope<ENTITY : Any>(
    private val context: MutableList<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> = mutableListOf()
) : List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> by context {

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.set(value: T?) {
        val right = Operand.Argument(this, value)
        context.add(this to right)
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.set(value: ColumnExpression<T, S>) {
        val right = Operand.Column(value)
        context.add(this to right)
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.setIfNotNull(value: T?) {
        if (value == null) return
        val right = Operand.Argument(this, value)
        context.add(this to right)
    }
}
