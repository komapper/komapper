package org.komapper.core.dsl.options

data class ScriptOptions(
    val separator: String = DEFAULT.separator,
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
) : QueryOptions {

    companion object {
        val DEFAULT = ScriptOptions(
            separator = ";",
            queryTimeoutSeconds = null,
            suppressLogging = false,
        )
    }
}
