package org.komapper.core.dsl.metamodel

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EntityMetamodelInstance(val entity: KClass<*>)
