package org.komapper.core.dsl.operator

import org.komapper.core.dsl.declaration.OnDeclaration

infix operator fun <E : Any> OnDeclaration<E>.plus(other: OnDeclaration<E>): OnDeclaration<E> {
    return {
        this@plus(this)
        other(this)
    }
}
