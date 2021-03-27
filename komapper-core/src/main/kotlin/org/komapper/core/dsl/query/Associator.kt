package org.komapper.core.dsl.query

fun interface Associator<T, S> {
    fun apply(entity1: T, entity2: S): T
}
