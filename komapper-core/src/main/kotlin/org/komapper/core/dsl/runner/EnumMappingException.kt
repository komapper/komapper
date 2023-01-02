package org.komapper.core.dsl.runner

import kotlin.reflect.KClass

class EnumMappingException(
    enumClass: KClass<out Enum<*>>,
    propertyName: String,
    value: Any,
    cause: Exception?,
) : RuntimeException(
    "Failed to map the value \"$value\" to the property \"$propertyName\" of the enum class \"${enumClass.qualifiedName}\".",
    cause,
)
