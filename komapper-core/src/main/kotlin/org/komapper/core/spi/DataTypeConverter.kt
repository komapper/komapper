package org.komapper.core.spi

import kotlin.reflect.KType

/**
 * A converter that converts a data type between an exterior type and an interior type.
 */
interface DataTypeConverter<EXTERIOR : Any, INTERIOR : Any> {
    /**
     * The exterior type.
     * [KType.isMarkedNullable] must be false.
     */
    val exteriorType: KType

    /**
     * The interior type.
     * [KType.isMarkedNullable] must be false.
     */
    val interiorType: KType

    /**
     * Converts the interior type to the exterior type.
     */
    fun wrap(interior: INTERIOR): EXTERIOR

    /**
     * Converts the exterior type to the interior type.
     */
    fun unwrap(exterior: EXTERIOR): INTERIOR
}
