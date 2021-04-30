package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@Scope
class ValuesScope<ENTITY : Any> internal constructor(
    private val context: MutableList<Pair<PropertyMetamodel<ENTITY, *, *>, Operand.ExteriorArgument<*, *>>> = mutableListOf()
) : List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand.ExteriorArgument<*, *>>> by context {

    companion object {
        operator fun <E : Any> ValuesDeclaration<E>.plus(other: ValuesDeclaration<E>): ValuesDeclaration<E> {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.set(value: T?) {
        val right = Operand.ExteriorArgument(this, value)
        context.add(this to right)
    }

    infix fun <T : Any, S : Any> PropertyMetamodel<ENTITY, T, S>.setIfNotNull(value: T?) {
        if (value == null) return
        val right = Operand.ExteriorArgument(this, value)
        context.add(this to right)
    }
}
