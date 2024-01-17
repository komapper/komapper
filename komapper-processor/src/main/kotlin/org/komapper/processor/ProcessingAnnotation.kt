package org.komapper.processor

import kotlin.reflect.KClass

internal data class ProcessingAnnotation(
    val annotationClass: KClass<*>,
    val definitionSourceResolver: EntityDefinitionSourceResolver,
    val requiresIdValidation: Boolean,
)
