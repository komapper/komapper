package org.komapper.core.dsl.element

import org.komapper.core.ThreadSafe

@ThreadSafe
fun interface Associator<T, S> {
    fun apply(entity1: T, entity2: S): T
}
