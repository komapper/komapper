package org.komapper.core.dsl.metamodel

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EntityMetamodelImplementor(val entity: KClass<*>)
