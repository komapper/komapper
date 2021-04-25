package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.ColumnExpression
import kotlin.reflect.KClass

interface PropertyMetamodel<E : Any, T : Any> : ColumnExpression<T> {
    val name: String
    val getter: (E) -> T?
    val setter: (E, T) -> E
    val nullable: Boolean
    val idAssignment: Assignment<E>?
}

class PropertyMetamodelImpl<E : Any, T : Any>(
    override val owner: EntityMetamodel<E, *, *>,
    private val descriptor: PropertyDescriptor<E, T>
) : PropertyMetamodel<E, T> {
    override val klass: KClass<T> get() = descriptor.klass
    override val name: String get() = descriptor.name
    override val columnName: String get() = descriptor.columnName
    override val alwaysQuote: Boolean get() = descriptor.alwaysQuote
    override val getter: (E) -> T? get() = descriptor.getter
    override val setter: (E, T) -> E get() = descriptor.setter
    override val nullable: Boolean = descriptor.nullable
    override val idAssignment: Assignment<E>? = descriptor.idAssignment
}

@Suppress("unused")
class PropertyMetamodelStub<E : Any, T : Any> : PropertyMetamodel<E, T> {
    override val owner: EntityMetamodel<E, *, *> get() = fail()
    override val klass: KClass<T> get() = fail()
    override val name: String get() = fail()
    override val columnName: String get() = fail()
    override val alwaysQuote: Boolean get() = fail()
    override val getter: (E) -> T? get() = fail()
    override val setter: (E, T) -> E get() = fail()
    override val nullable: Boolean get() = fail()
    override val idAssignment: Assignment<E> get() = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}
