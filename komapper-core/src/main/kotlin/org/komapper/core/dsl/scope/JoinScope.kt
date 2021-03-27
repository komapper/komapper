package org.komapper.core.dsl.scope

import org.komapper.core.Scope
import org.komapper.core.dsl.context.JoinContext
import org.komapper.core.dsl.data.Criterion
import org.komapper.core.dsl.data.Operand
import org.komapper.core.metamodel.PropertyMetamodel

@Scope
class JoinScope<ENTITY> internal constructor(private val context: JoinContext) {

    companion object {
        operator fun <ENTITY> JoinDeclaration<ENTITY>.plus(other: JoinDeclaration<ENTITY>): JoinDeclaration<ENTITY> {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Eq(Operand.Column(this), Operand.Column(right)))
    }
}
