package org.komapper.core

import kotlin.reflect.KClass

/**
 * The value bound to the SQL statement.
 *
 * @property any the value
 * @property klass the class of the value
 * @property masking whether the value is masked or not in log
 */
@ThreadSafe
data class Value(val any: Any?, val klass: KClass<*>, val masking: Boolean = false) {
    constructor(any: Any) : this(any, any::class)
}
