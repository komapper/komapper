package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import org.komapper.core.Value
import org.komapper.core.dsl.expression.PropertyExpression
import kotlin.reflect.KType

/**
 * Represents a property metamodel that maps an entity type to exterior and interior types.
 *
 * @param ENTITY the entity type
 * @param EXTERIOR the exterior type
 * @param INTERIOR the interior type
 */
@ThreadSafe
interface PropertyMetamodel<ENTITY : Any, EXTERIOR : Any, INTERIOR : Any> : PropertyExpression<EXTERIOR, INTERIOR> {
    override val owner: EntityMetamodel<ENTITY, *, *>

    /**
     * The name of the property.
     */
    val name: String

    /**
     * A function that retrieves the exterior type value from the given entity.
     *
     * @param ENTITY the entity type
     * @return the exterior type value or null if not present
     */
    val getter: (ENTITY) -> EXTERIOR?

    /**
     * A function that sets the exterior type value to the given entity.
     *
     * @param ENTITY the entity type
     * @param EXTERIOR the exterior type
     * @return the updated entity
     */
    val setter: (ENTITY, EXTERIOR) -> ENTITY

    /**
     * Indicates whether the property is nullable.
     */
    val nullable: Boolean

    /**
     * Converts the given entity to a `Value` object containing the interior type.
     *
     * @param entity the entity to convert
     * @return a `Value` object containing the interior type
     */
    fun toValue(entity: ENTITY): Value<INTERIOR> {
        val exterior = getter(entity)
        val interior = if (exterior == null) null else unwrap(exterior)
        return Value(interior, interiorType, masking)
    }
}

@Suppress("unused")
class PropertyMetamodelImpl<ENTITY : Any, EXTERIOR : Any, INTERIOR : Any>(
    override val owner: EntityMetamodel<ENTITY, *, *>,
    private val descriptor: PropertyDescriptor<ENTITY, EXTERIOR, INTERIOR>,
) : PropertyMetamodel<ENTITY, EXTERIOR, INTERIOR> {
    override val exteriorType: KType = descriptor.exteriorType
    override val interiorType: KType = descriptor.interiorType
    override val name: String get() = descriptor.name
    override val columnName: String get() = descriptor.columnName
    override val alwaysQuote: Boolean get() = descriptor.alwaysQuote
    override val masking: Boolean get() = descriptor.masking
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
    override val exteriorType: KType get() = fail()
    override val interiorType: KType get() = fail()
    override val name: String get() = fail()
    override val columnName: String get() = fail()
    override val alwaysQuote: Boolean get() = fail()
    override val masking: Boolean get() = fail()
    override val getter: (ENTITY) -> EXTERIOR? get() = fail()
    override val setter: (ENTITY, EXTERIOR) -> ENTITY get() = fail()
    override val wrap: (EXTERIOR) -> EXTERIOR get() = fail()
    override val unwrap: (EXTERIOR) -> EXTERIOR get() = fail()
    override val nullable: Boolean get() = fail()

    private fun fail(): Nothing {
        error("Fix google/ksp compile errors.")
    }
}
