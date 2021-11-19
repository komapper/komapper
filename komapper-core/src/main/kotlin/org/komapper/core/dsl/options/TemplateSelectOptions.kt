package org.komapper.core.dsl.options

data class TemplateSelectOptions(
    val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : FetchOptions {

    companion object {
        val default = TemplateSelectOptions(
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
