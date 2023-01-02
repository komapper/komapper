package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.HavingDeclaration

infix operator fun HavingDeclaration.plus(other: HavingDeclaration): HavingDeclaration {
    return {
        this@plus(this)
        other(this)
    }
}

fun HavingDeclaration.and(other: HavingDeclaration): HavingDeclaration {
    return {
        this@and(this)
        and { other(this) }
    }
}

fun HavingDeclaration.or(other: HavingDeclaration): HavingDeclaration {
    return {
        this@or(this)
        or { other(this) }
    }
}
