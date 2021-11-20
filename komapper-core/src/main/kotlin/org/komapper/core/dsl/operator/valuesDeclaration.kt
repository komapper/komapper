package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.ValuesDeclaration

infix operator fun <E : Any> ValuesDeclaration<E>.plus(other: ValuesDeclaration<E>): ValuesDeclaration<E> {
    return {
        this@plus(this)
        other(this)
    }
}
