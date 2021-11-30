package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.AssignmentDeclaration

infix operator fun <ENTITY : Any, META> AssignmentDeclaration<ENTITY, META>.plus(other: AssignmentDeclaration<ENTITY, META>): AssignmentDeclaration<ENTITY, META> {
    return { metamodel ->
        this@plus(this, metamodel)
        other(this, metamodel)
    }
}
