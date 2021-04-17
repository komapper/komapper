package org.komapper.core.dsl.metamodel

import kotlin.reflect.KClass

class PropertyDescriptor<E, T : Any>(
    val klass: KClass<T>,
    val name: String,
    val columnName: String,
    val alwaysQuote: Boolean,
    val getter: (E) -> T?,
    val setter: (E, T) -> E,
    val nullable: Boolean,
    val idAssignment: Assignment<E>?
)
