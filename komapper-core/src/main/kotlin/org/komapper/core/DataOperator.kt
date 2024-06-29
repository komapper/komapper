package org.komapper.core

import kotlin.reflect.KClass
import kotlin.reflect.KType

@ThreadSafe
interface DataOperator {
    /**
     * Formats the value.
     *
     * @param value the value
     * @param type the type of the value
     * @param masking whether to mask the value
     * @return the formatted value
     */
    fun <T : Any> formatValue(value: T?, type: KType, masking: Boolean): String

    /**
     * Returns the data type name.
     *
     * @param type the type corresponding the data type
     * @return the data type name
     */
    fun <T : Any> getDataTypeName(type: KType): String
}

object DryRunDataOperator : DataOperator {

    override fun <T : Any> formatValue(value: T?, type: KType, masking: Boolean): String {
        return if (masking) {
            "*****"
        } else if (value == null) {
            "null"
        } else {
            when (type.classifier as KClass<*>) {
                String::class -> "'$value'"
                else -> value.toString()
            }
        }
    }

    override fun <T : Any> getDataTypeName(type: KType): String {
        throw UnsupportedOperationException()
    }
}
