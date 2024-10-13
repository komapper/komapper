package org.komapper.core.dsl.options

import org.komapper.core.Dialect

data class TemplateExecuteOptions(
    /**
     * The escape sequence to be used in the LIKE predicate.
     * If null is returned, the value of [Dialect.escapeSequence] will be used.
     */
    val escapeSequence: String? = DEFAULT.escapeSequence,
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
) : QueryOptions {
    companion object {
        val DEFAULT = TemplateExecuteOptions(
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false,
        )
    }
}
