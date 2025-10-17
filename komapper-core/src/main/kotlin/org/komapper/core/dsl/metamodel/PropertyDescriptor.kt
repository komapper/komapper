package org.komapper.core.dsl.metamodel

import org.komapper.core.ThreadSafe
import kotlin.reflect.KType

@ThreadSafe
class PropertyDescriptor<ENTITY, EXTERIOR : Any, INTERIOR : Any>(
    val exteriorType: KType,
    val interiorType: KType,
    val name: String,
    val columnName: String,
    val alwaysQuote: Boolean,
    val masking: Boolean,
    val updatable: Boolean,
    val insertable: Boolean,
    val getter: (ENTITY) -> EXTERIOR?,
    val setter: (ENTITY, EXTERIOR) -> ENTITY,
    val wrap: (INTERIOR) -> EXTERIOR,
    val unwrap: (EXTERIOR) -> INTERIOR,
    val nullable: Boolean,
    val length: Int?,
    val precision: Int?,
    val scale: Int?,
)
