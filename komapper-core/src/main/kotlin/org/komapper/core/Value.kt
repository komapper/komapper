package org.komapper.core

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * The value bound to the SQL statement.
 *
 * @property any the value
 * @property type the type of the value
 * @property masking whether the value is masked or not in log
 * @property hint the additional hint for the value type
 */
@ThreadSafe
data class Value<T : Any>(val any: T?, val type: KType, val masking: Boolean = false, val hint: Any? = null) {
    init {
        require(!type.isMarkedNullable) { "The type must not be nullable." }
        require(type.classifier as? KClass<*> != null) { "The type must be a class." }
    }
}
