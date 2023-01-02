package org.komapper.core.dsl.options

data class ScriptOptions(
    val separator: String,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val DEFAULT = ScriptOptions(
            separator = ";",
            queryTimeoutSeconds = null,
            suppressLogging = false,
        )
    }
}
