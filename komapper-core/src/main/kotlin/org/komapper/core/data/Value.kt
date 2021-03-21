package org.komapper.core.data

import kotlin.reflect.KClass

data class Value(val any: Any?, val klass: KClass<*>) {
    constructor(any: Any) : this(any, any::class)
}
