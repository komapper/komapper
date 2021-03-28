package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.context.ValuesContext
import org.komapper.core.dsl.data.Operand
import org.komapper.core.metamodel.ColumnInfo

@Scope
class ValuesScope internal constructor(
    internal val context: ValuesContext = ValuesContext()
) {

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
}
