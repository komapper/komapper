package org.komapper.core.dsl.options

data class InsertOptions(
    override val batchSize: Int?,
    val disableSequenceAssignment: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : BatchOptions, QueryOptions {

    companion object {
        val default = InsertOptions(
            batchSize = null,
            disableSequenceAssignment = false,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
