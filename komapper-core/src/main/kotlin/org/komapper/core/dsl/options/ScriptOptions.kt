package org.komapper.core.dsl.options

data class ScriptOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = ScriptOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
