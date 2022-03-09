package org.komapper.core.dsl.query

import org.komapper.core.Value

/**
 * Represents a binder that binds given values to an SQL template.
 */
interface TemplateBinder<BINDER : TemplateBinder<BINDER>> {

    /**
     * Bind a value.
     *
     * @param name the value name
     * @param value the value
     * @return the binder
     */
    fun bindValue(name: String, value: Value<*>): BINDER
}

/**
 * Bind a value.
 *
 * @param T the type of the value
 * @param name the value name
 * @param value the value
 * @param masking whether the value is masked or not in log
 * @return the binder
 */
inline fun <reified T : Any, B : TemplateBinder<B>> TemplateBinder<B>.bind(name: String, value: T?, masking: Boolean = false): B {
    val v = Value(value, T::class, masking)
    return this.bindValue(name, v)
}
