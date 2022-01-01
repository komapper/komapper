package org.komapper.core.dsl.options

data class InsertOptions(
    override val batchSize: Int?,
    /**
     * Whether to disable assigning sequence-numbered values to primary keys.
     */
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
