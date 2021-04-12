package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@Scope
class ValuesScope<ENTITY : Any> internal constructor(
    private val context: MutableList<Pair<Operand.Property, Operand.Parameter>> = mutableListOf()
) : Collection<Pair<Operand.Property, Operand.Parameter>> by context {

    companion object {
        operator fun <E : Any> ValuesDeclaration<E>.plus(other: ValuesDeclaration<E>): ValuesDeclaration<E> {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> PropertyMetamodel<ENTITY, T>.set(value: T?) {
        val left = Operand.Property(this)
        val right = Operand.Parameter(this, value)
        context.add(left to right)
    }
}
