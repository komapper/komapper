package org.komapper.core.dsl.runner

import kotlin.reflect.KClass

class PropertyMappingException(
    entityClass: KClass<*>,
    propertyName: String,
    cause: Exception,
) : RuntimeException("Failed to map a value to the property \"$propertyName\" of the entity class \"${entityClass.qualifiedName}\".", cause)
