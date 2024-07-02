package org.komapper.core.spi

import kotlin.reflect.KType

interface DataTypeConverter<EXTERIOR : Any, INTERIOR : Any> {

    val exteriorType: KType

    val interiorType: KType

    fun wrap(interior: INTERIOR): EXTERIOR

    fun unwrap(exterior: EXTERIOR): INTERIOR
}
