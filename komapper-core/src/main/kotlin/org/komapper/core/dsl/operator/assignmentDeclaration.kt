package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AssignmentDeclaration

infix operator fun <ENTITY : Any> AssignmentDeclaration<ENTITY>.plus(other: AssignmentDeclaration<ENTITY>): AssignmentDeclaration<ENTITY> {
    return {
        this@plus(this)
        other(this)
    }
}
