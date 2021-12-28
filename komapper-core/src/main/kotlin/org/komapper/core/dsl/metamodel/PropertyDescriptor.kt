package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import kotlin.reflect.KClass

@ThreadSafe
class PropertyDescriptor<ENTITY, EXTERIOR : Any, INTERIOR : Any>(
    val exteriorClass: KClass<EXTERIOR>,
    val interiorClass: KClass<INTERIOR>,
    val name: String,
    val columnName: String,
    val alwaysQuote: Boolean,
    val masking: Boolean,
    val getter: (ENTITY) -> EXTERIOR?,
    val setter: (ENTITY, EXTERIOR) -> ENTITY,
    val wrap: (INTERIOR) -> EXTERIOR,
    val unwrap: (EXTERIOR) -> INTERIOR,
    val nullable: Boolean,
)
