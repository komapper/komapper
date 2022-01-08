package org.komapper.core.dsl.options

data class SchemaOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val DEFAULT = SchemaOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
