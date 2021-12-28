package org.komapper.core

import kotlin.reflect.KClass

@ThreadSafe
data class Value(val any: Any?, val klass: KClass<*>, val masking: Boolean = false) {
    constructor(any: Any) : this(any, any::class)
}
