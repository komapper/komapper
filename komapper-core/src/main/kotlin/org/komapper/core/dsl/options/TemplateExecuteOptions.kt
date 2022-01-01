package org.komapper.core.dsl.options

import org.komapper.core.Dialect

data class TemplateExecuteOptions(
    /**
     * The escape sequence to be used in the LIKE predicate.
     * If null is returned, the value of [Dialect.escapeSequence] will be used.
     */
    val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = TemplateExecuteOptions(
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
