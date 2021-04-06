package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.expr.PropertyExpression

@Scope
class ValuesScope internal constructor(
    private val context: MutableList<Pair<Operand.Property, Operand.Parameter>> = mutableListOf()
) : Collection<Pair<Operand.Property, Operand.Parameter>> by context {

    companion object {
        operator fun ValuesDeclaration.plus(other: ValuesDeclaration): ValuesDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> PropertyExpression<T>.set(value: T?) {
        val left = Operand.Property(this)
        val right = Operand.Parameter(this, value)
        context.add(left to right)
    }
}
