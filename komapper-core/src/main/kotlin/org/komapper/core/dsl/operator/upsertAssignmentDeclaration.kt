package org.komapper.core.dsl.operator

import org.komapper.core.dsl.expression.UpsertAssignmentDeclaration

infix operator fun <ENTITY : Any, META> UpsertAssignmentDeclaration<ENTITY, META>.plus(other: UpsertAssignmentDeclaration<ENTITY, META>): UpsertAssignmentDeclaration<ENTITY, META> {
    return { metamodel ->
        this@plus(this, metamodel)
        other(this, metamodel)
    }
}
