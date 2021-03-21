package org.komapper.core.metamodel

import kotlin.reflect.KClass

interface PropertyMetamodel<E, T : Any> {
    val owner: EntityMetamodel<E>
    val klass: KClass<T>
    val columnName: String
    val get: (E) -> T?
    val getWithUncheckedCast: (Any) -> T?
    val set: (Pair<E, T>) -> E
}

class PropertyMetamodelImpl<E, T : Any>(
    override val owner: EntityMetamodel<E>,
    private val descriptor: PropertyDescriptor<E, T>
) : PropertyMetamodel<E, T> {
    override val klass: KClass<T> get() = descriptor.klass
    override val columnName: String get() = descriptor.columnName
    override val get: (E) -> T? get() = descriptor.get
    override val getWithUncheckedCast: (Any) -> T?
        get() = {
            @Suppress("UNCHECKED_CAST")
            descriptor.get(it as E)
        }
    override val set: (Pair<E, T>) -> E get() = descriptor.set
}

class EmptyPropertyMetamodel<E, T : Any> : PropertyMetamodel<E, T> {
    override val owner: EntityMetamodel<E> get() = error("error")
    override val klass: KClass<T> get() = error("error")
    override val columnName: String get() = error("error")
    override val get: (E) -> T? get() = error("error")
    override val getWithUncheckedCast: (Any) -> T? get() = error("error")
    override val set: (Pair<E, T>) -> E get() = error("error")
}
