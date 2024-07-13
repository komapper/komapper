package org.komapper.core.dsl.options

import org.komapper.core.Dialect

data class TemplateSelectOptions(
    /**
     * The escape sequence to be used in the LIKE predicate.
     * If null is returned, the value of [Dialect.escapeSequence] will be used.
     */
    val escapeSequence: String? = DEFAULT.escapeSequence,
    override val fetchSize: Int? = DEFAULT.fetchSize,
    override val maxRows: Int? = DEFAULT.maxRows,
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
) : FetchOptions {

    companion object {
        val DEFAULT = TemplateSelectOptions(
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false,
        )
    }
}
