package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.WhenDeclaration

infix operator fun WhenDeclaration.plus(other: WhenDeclaration): WhenDeclaration {
    return {
        this@plus(this)
        other(this)
    }
}

fun WhenDeclaration.and(other: WhenDeclaration): WhenDeclaration {
    return {
        this@and(this)
        and { other(this) }
    }
}

fun WhenDeclaration.or(other: WhenDeclaration): WhenDeclaration {
    return {
        this@or(this)
        or { other(this) }
    }
}
