package org.komapper.core.spi

import kotlin.reflect.KClass

interface DataConverter<EXTERIOR : Any, INTERIOR : Any> {

    val exteriorClass: KClass<EXTERIOR>

    val interiorClass: KClass<INTERIOR>

    fun wrap(interior: INTERIOR): EXTERIOR

    fun unwrap(exterior: EXTERIOR): INTERIOR
}
