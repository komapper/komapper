package org.komapper.core.dsl.operator

import org.komapper.core.dsl.declaration.WhereDeclaration

infix operator fun WhereDeclaration.plus(other: WhereDeclaration): WhereDeclaration {
    return {
        this@plus(this)
        other(this)
    }
}

infix fun WhereDeclaration.and(other: WhereDeclaration): WhereDeclaration {
    return {
        this@and(this)
        and { other(this) }
    }
}

infix fun WhereDeclaration.or(other: WhereDeclaration): WhereDeclaration {
    return {
        this@or(this)
        or { other(this) }
    }
}
