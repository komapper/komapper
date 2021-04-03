package org.komapper.core.metamodel

import kotlin.reflect.KClass

class PropertyDescriptor<E, T : Any>(
    val klass: KClass<T>,
    val columnName: String,
    val getter: (E) -> T?,
    val setter: (E, T) -> E,
    val nullable: Boolean,
    val idGenerator: IdGeneratorDescriptor<E, T>?
) {
    val idAssignment: Assignment<E>? = idGenerator?.createAssignment(setter)
}
