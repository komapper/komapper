package org.komapper.processor

import kotlin.reflect.KClass

internal data class ProcessingAnnotation(
    val annotationClass: KClass<*>,
    val createEntityDefinitionSourceResolver: (context: Context) -> EntityDefinitionSourceResolver,
    val requiresIdValidation: Boolean,
)
