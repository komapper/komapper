package org.komapper.core.dsl.element

import org.komapper.core.dsl.metamodel.PropertyMetamodel

sealed class UpdateSet<ENTITY : Any> {
    data class Pairs<ENTITY : Any>(val pairs: List<Pair<Operand.Property, Operand>>) : UpdateSet<ENTITY>()
    data class Properties<ENTITY : Any>(val properties: List<PropertyMetamodel<ENTITY, *>>) : UpdateSet<ENTITY>()
}
