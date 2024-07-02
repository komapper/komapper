package org.komapper.core.dsl.runner

import kotlin.reflect.KType

class EnumMappingException(
    enumType: KType,
    propertyName: String,
    value: Any,
    cause: Exception?,
) : RuntimeException(
    "Failed to map the value \"$value\" to the property \"$propertyName\" of the enum class \"${enumType}\".",
    cause,
)
