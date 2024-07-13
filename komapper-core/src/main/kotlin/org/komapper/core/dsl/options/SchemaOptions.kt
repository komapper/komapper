package org.komapper.core.dsl.options

data class SchemaOptions(
    override val queryTimeoutSeconds: Int? = DEFAULT.queryTimeoutSeconds,
    override val suppressLogging: Boolean = DEFAULT.suppressLogging,
) : QueryOptions {

    companion object {
        val DEFAULT = SchemaOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false,
        )
    }
}
