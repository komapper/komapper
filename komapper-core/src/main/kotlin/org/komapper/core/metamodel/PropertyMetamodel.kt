package org.komapper.core.metamodel

import kotlin.reflect.KClass

interface PropertyMetamodel<E, T : Any> : ColumnInfo<T> {
    val owner: EntityMetamodel<E>
    val getter: (E) -> T?
    val getterWithUncheckedCast: (Any) -> T?
    val setter: (E, T) -> E
}

class PropertyMetamodelImpl<E, T : Any>(
    override val owner: EntityMetamodel<E>,
    private val descriptor: PropertyDescriptor<E, T>
) : PropertyMetamodel<E, T> {
    override val klass: KClass<T> get() = descriptor.klass
    override val columnName: String get() = descriptor.columnName
    override val getter: (E) -> T? get() = descriptor.getter
    override val getterWithUncheckedCast: (Any) -> T?
        get() = {
            @Suppress("UNCHECKED_CAST")
            descriptor.getter(it as E)
        }
    override val setter: (E, T) -> E get() = descriptor.setter
}

class EmptyPropertyMetamodel<E, T : Any> : PropertyMetamodel<E, T> {
    override val owner: EntityMetamodel<E> get() = error("error")
    override val klass: KClass<T> get() = error("error")
    override val columnName: String get() = error("error")
    override val getter: (E) -> T? get() = error("error")
    override val getterWithUncheckedCast: (Any) -> T? get() = error("error")
    override val setter: (E, T) -> E get() = error("error")
}
