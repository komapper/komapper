package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.expression.PropertyExpression
import kotlin.reflect.KClass

@ThreadSafe
interface PropertyMetamodel<ENTITY : Any, EXTERIOR : Any, INTERIOR : Any> : PropertyExpression<EXTERIOR, INTERIOR> {
    override val owner: EntityMetamodel<ENTITY, *, *>
    val name: String
    val getter: (ENTITY) -> EXTERIOR?
    val setter: (ENTITY, EXTERIOR) -> ENTITY
    val nullable: Boolean

    fun toValue(entity: ENTITY): Value {
        val exterior = getter(entity)
        val interior = if (exterior == null) null else unwrap(exterior)
        return Value(interior, interiorClass)
    }
}

class PropertyMetamodelImpl<ENTITY : Any, EXTERIOR : Any, INTERIOR : Any>(
    override val owner: EntityMetamodel<ENTITY, *, *>,
    private val descriptor: PropertyDescriptor<ENTITY, EXTERIOR, INTERIOR>
) : PropertyMetamodel<ENTITY, EXTERIOR, INTERIOR> {
    override val exteriorClass: KClass<EXTERIOR> get() = descriptor.exteriorClass
    override val interiorClass: KClass<INTERIOR> get() = descriptor.interiorClass
    override val name: String get() = descriptor.name
    override val columnName: String get() = descriptor.columnName
    override val alwaysQuote: Boolean get() = descriptor.alwaysQuote
    override val getter: (ENTITY) -> EXTERIOR? get() = descriptor.getter
    override val setter: (ENTITY, EXTERIOR) -> ENTITY get() = descriptor.setter
    override val wrap: (INTERIOR) -> EXTERIOR get() = descriptor.wrap
    override val unwrap: (EXTERIOR) -> INTERIOR get() = descriptor.unwrap
    override val nullable: Boolean get() = descriptor.nullable
}

@Suppress("unused")
class PropertyMetamodelStub<ENTITY : Any, EXTERIOR : Any> :
    PropertyMetamodel<ENTITY, EXTERIOR, EXTERIOR> {
    override val owner: EntityMetamodel<ENTITY, *, *> get() = fail()
    override val exteriorClass: KClass<EXTERIOR> get() = fail()
    override val interiorClass: KClass<EXTERIOR> get() = fail()
    override val name: String get() = fail()
    override val columnName: String get() = fail()
    override val alwaysQuote: Boolean get() = fail()
    override val getter: (ENTITY) -> EXTERIOR? get() = fail()
    override val setter: (ENTITY, EXTERIOR) -> ENTITY get() = fail()
    override val wrap: (EXTERIOR) -> EXTERIOR get() = fail()
    override val unwrap: (EXTERIOR) -> EXTERIOR get() = fail()
    override val nullable: Boolean get() = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}
