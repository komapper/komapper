package org.komapper.core.dsl.element

sealed class UpdateSet<ENTITY : Any> {
    data class Pairs<ENTITY : Any>(val pairs: List<Pair<Operand.Property, Operand>>) : UpdateSet<ENTITY>()
}
