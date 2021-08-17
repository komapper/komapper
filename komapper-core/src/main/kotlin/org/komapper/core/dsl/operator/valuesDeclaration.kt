package org.komapper.core.dsl.operator

import org.komapper.core.dsl.declaration.ValuesDeclaration

infix operator fun <E : Any> ValuesDeclaration<E>.plus(other: ValuesDeclaration<E>): ValuesDeclaration<E> {
    return {
        this@plus(this)
        other(this)
    }
}
