package org.komapper.core.metamodel

import kotlin.reflect.KClass

class PropertyDescriptor<E, T : Any>(
    val klass: KClass<T>,
    val columnName: String,
    val get: (E) -> T?,
    val set: (Pair<E, T>) -> E
)
