package org.komapper.core.dsl.metamodel

import kotlin.reflect.KClass

class PropertyDescriptor<ENTITY, EXTERIOR : Any, INTERIOR : Any>(
    val klass: KClass<EXTERIOR>,
    val interiorClass: KClass<INTERIOR>,
    val name: String,
    val columnName: String,
    val alwaysQuote: Boolean,
    val getter: (ENTITY) -> EXTERIOR?,
    val setter: (ENTITY, EXTERIOR) -> ENTITY,
    val compose: (INTERIOR) -> EXTERIOR,
    val decompose: (EXTERIOR) -> INTERIOR,
    val nullable: Boolean,
    val idAssignment: Assignment<ENTITY>?
)
