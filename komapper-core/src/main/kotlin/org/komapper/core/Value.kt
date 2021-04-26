package org.komapper.core

import kotlin.reflect.KClass

data class Value(val any: Any?, val klass: KClass<*>) {
    constructor(any: Any) : this(any, any::class)
}
