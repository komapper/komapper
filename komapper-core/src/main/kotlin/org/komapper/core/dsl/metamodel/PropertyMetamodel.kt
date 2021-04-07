package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.PropertyExpression
import kotlin.reflect.KClass

interface PropertyMetamodel<E : Any, T : Any> : PropertyExpression<T> {
    override val owner: EntityMetamodel<E>
    val getter: (E) -> T?
    val getterWithUncheckedCast: (Any) -> T?
    val setter: (E, T) -> E
    val nullable: Boolean
    val idAssignment: Assignment<E>?
}

class PropertyMetamodelImpl<E : Any, T : Any>(
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
    override val nullable: Boolean = descriptor.nullable
    override val idAssignment: Assignment<E>? = descriptor.idAssignment
}

@Suppress("unused")
class EmptyPropertyMetamodel<E : Any, T : Any> : PropertyMetamodel<E, T> {
    override val owner: EntityMetamodel<E> get() = fail()
    override val klass: KClass<T> get() = fail()
    override val columnName: String get() = fail()
    override val getter: (E) -> T? get() = fail()
    override val getterWithUncheckedCast: (Any) -> T? get() = fail()
    override val setter: (E, T) -> E get() = fail()
    override val nullable: Boolean get() = fail()
    override val idAssignment: Assignment<E> get() = fail()

    private fun fail(): Nothing {
        error("Fix a compile error.")
    }
}
