package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.element.Operand
import org.komapper.core.metamodel.ColumnInfo

@Scope
class SetScope internal constructor(
    private val context: MutableList<Pair<Operand.Column, Operand>> = mutableListOf()
) : List<Pair<Operand.Column, Operand>> by context {

    companion object {
        operator fun ValuesDeclaration.plus(other: ValuesDeclaration): ValuesDeclaration {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> ColumnInfo<T>.set(value: T?) {
        val left = Operand.Column(this)
        val right = Operand.Parameter(this, value)
        context.add(left to right)
    }

    infix fun <T : Any> ColumnInfo<T>.set(operand: ColumnInfo<T>) {
        val left = Operand.Column(this)
        val right = Operand.Column(operand)
        context.add(left to right)
    }
}
