package org.komapper.core.dsl.options

data class TemplateExecuteOptions(
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
