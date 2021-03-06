package org.komapper.core.dsl.operator

import org.komapper.core.dsl.declaration.SetDeclaration

infix operator fun <E : Any> SetDeclaration<E>.plus(other: SetDeclaration<E>): SetDeclaration<E> {
    return {
        this@plus(this)
        other(this)
    }
}
