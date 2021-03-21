package org.komapper.core.query.scope

import org.komapper.core.Scope
import org.komapper.core.metamodel.PropertyMetamodel
import org.komapper.core.query.context.JoinContext
import org.komapper.core.query.data.Criterion
import org.komapper.core.query.data.Operand

@Scope
class JoinScope<ENTITY> internal constructor(private val context: JoinContext<ENTITY>) {

    companion object {
        operator fun <ENTITY> JoinDeclaration<ENTITY>.plus(other: JoinDeclaration<ENTITY>): JoinDeclaration<ENTITY> {
            return {
                this@plus(this)
                other(this)
            }
        }
    }

    infix fun <T : Any> PropertyMetamodel<*, T>.eq(right: PropertyMetamodel<ENTITY, T>) {
        context.add(Criterion.Eq(Operand.Property(this), Operand.Property(right)))
    }
}
