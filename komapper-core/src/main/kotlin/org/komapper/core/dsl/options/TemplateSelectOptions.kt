package org.komapper.core.dsl.options

import org.komapper.core.Dialect

data class TemplateSelectOptions(
    /**
     * The escape sequence to be used in the LIKE predicate.
     * If null is returned, the value of [Dialect.escapeSequence] will be used.
     */
    val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
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
