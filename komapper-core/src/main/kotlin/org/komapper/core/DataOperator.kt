package org.komapper.core

import kotlin.reflect.KClass

@ThreadSafe
interface DataOperator {
    /**
     * Formats the value.
     *
     * @param value the value
     * @param valueClass the class of the value
     * @param masking whether to mask the value
     * @return the formatted value
     */
    fun <T : Any> formatValue(value: T?, valueClass: KClass<out T>, masking: Boolean): String

    /**
     * Returns the data type name.
     *
     * @param klass the class corresponding the data type
     * @return the data type name
     */
    fun getDataTypeName(klass: KClass<*>): String
}

object DryRunDataOperator : DataOperator {

    override fun <T : Any> formatValue(value: T?, valueClass: KClass<out T>, masking: Boolean): String {
        return if (masking) {
            "*****"
        } else if (value == null) {
            "null"
        } else {
            when (valueClass) {
                String::class -> "'$value'"
                else -> value.toString()
            }
        }
    }

    override fun getDataTypeName(klass: KClass<*>): String {
        throw UnsupportedOperationException()
    }
}
