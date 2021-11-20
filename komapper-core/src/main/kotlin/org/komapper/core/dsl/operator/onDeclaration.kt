package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.OnDeclaration

infix operator fun <E : Any> OnDeclaration.plus(other: OnDeclaration): OnDeclaration {
    return {
        this@plus(this)
        other(this)
    }
}
